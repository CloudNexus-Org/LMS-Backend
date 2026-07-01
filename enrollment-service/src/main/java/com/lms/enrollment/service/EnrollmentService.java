package com.lms.enrollment.service;

import com.lms.enrollment.dto.*;
import com.lms.enrollment.event.EnrollmentEventProducer;
import com.lms.enrollment.model.*;
import com.lms.enrollment.repository.EnrollmentRepository;
import com.lms.enrollment.repository.LessonProgressRepository;
import com.lms.enrollment.repository.TrackProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TrackProgressRepository trackProgressRepository;
    private final EnrollmentEventProducer eventProducer;

    @Transactional
    public EnrollmentDetailResponse enroll(Long userId, EnrollRequest request) {
        if (request.getTrackId() == null || request.getTrackId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        if (enrollmentRepository.existsByUserIdAndTrackId(userId, request.getTrackId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this track");
        }
        Long courseId = request.getCourseId() != null
                ? request.getCourseId()
                : CatalogMetadata.courseIdForTrack(request.getTrackId());
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userId(userId)
                .trackId(request.getTrackId())
                .courseId(courseId)
                .status("ACTIVE")
                .build());
        initTrackProgress(userId, request.getTrackId());
        eventProducer.publishEnrollmentCreated(userId, request.getTrackId(), enrollment.getId());
        return toDetail(enrollment);
    }

    @Transactional
    public void enrollFromPayment(Long userId, String trackId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndTrackId(userId, trackId)) {
            return;
        }
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userId(userId)
                .trackId(trackId)
                .courseId(courseId != null ? courseId : CatalogMetadata.courseIdForTrack(trackId))
                .status("ACTIVE")
                .build());
        initTrackProgress(userId, trackId);
        eventProducer.publishEnrollmentCreated(userId, trackId, enrollment.getId());
    }

    public List<MyCourseResponse> myEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .map(this::toMyCourse)
                .toList();
    }

    public EnrollmentDetailResponse myEnrollmentByTrack(Long userId, String trackId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndTrackId(userId, trackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        return toDetail(enrollment);
    }

    public CourseProgressResponse courseProgress(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No enrollment for course"));
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, enrollment.getTrackId())
                .orElse(null);
        List<Long> completedIds = lessonProgressRepository
                .findByUserIdAndTrackIdAndCompletedTrue(userId, enrollment.getTrackId()).stream()
                .map(LessonProgress::getLessonId)
                .toList();
        return CourseProgressResponse.builder()
                .courseId(courseId)
                .trackId(enrollment.getTrackId())
                .progress(tp != null ? tp.getProgressPct() : 0)
                .completedLessons(tp != null ? tp.getCompletedLessons() : 0)
                .totalLessons(tp != null ? tp.getTotalLessons() : CatalogMetadata.totalLessonsForTrack(enrollment.getTrackId()))
                .completedLessonIds(completedIds)
                .build();
    }

    @Transactional
    public LessonProgress updateLessonProgress(Long userId, Long lessonId, LessonProgressRequest request) {
        if (request.getTrackId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        requireEnrollment(userId, request.getTrackId());
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .trackId(request.getTrackId())
                        .build());
        if (request.getWatchDurationSec() != null) {
            progress.setWatchDurationSec(request.getWatchDurationSec());
        }
        return lessonProgressRepository.save(progress);
    }

    public TrackProgressResponse trackProgress(Long userId, String trackId) {
        requireEnrollment(userId, trackId);
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, trackId)
                .orElseGet(() -> TrackProgress.builder()
                        .id(new TrackProgressId(userId, trackId))
                        .totalLessons(CatalogMetadata.totalLessonsForTrack(trackId))
                        .build());
        return TrackProgressResponse.builder()
                .trackId(trackId)
                .progress(tp.getProgressPct())
                .completedLessons(tp.getCompletedLessons())
                .totalLessons(tp.getTotalLessons())
                .lastLessonId(tp.getLastLessonId())
                .build();
    }

    @Transactional
    public TrackProgressResponse completeLesson(Long userId, Long lessonId, CompleteLessonRequest request) {
        if (request.getTrackId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        requireEnrollment(userId, request.getTrackId());
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .trackId(request.getTrackId())
                        .build());
        if (!Boolean.TRUE.equals(progress.getCompleted())) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());
            lessonProgressRepository.save(progress);
            eventProducer.publishLessonCompleted(userId, request.getTrackId(), lessonId);
        }
        TrackProgress tp = recalculateTrackProgress(userId, request.getTrackId(), lessonId);
        if (tp.getProgressPct() >= 100) {
            Enrollment enrollment = enrollmentRepository.findByUserIdAndTrackId(userId, request.getTrackId()).orElseThrow();
            enrollment.setStatus("COMPLETED");
            enrollmentRepository.save(enrollment);
            eventProducer.publishTrackCompleted(userId, request.getTrackId());
        } else if (tp.getProgressPct() == 50 || tp.getProgressPct() == 75) {
            eventProducer.publishProgressCompleted(userId, request.getTrackId(), tp.getProgressPct());
        }
        return TrackProgressResponse.builder()
                .trackId(request.getTrackId())
                .progress(tp.getProgressPct())
                .completedLessons(tp.getCompletedLessons())
                .totalLessons(tp.getTotalLessons())
                .lastLessonId(tp.getLastLessonId())
                .build();
    }

    public StudentDashboardResponse studentDashboard(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
        long inProgress = enrollments.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        long completed = enrollments.stream().filter(e -> "COMPLETED".equals(e.getStatus())).count();
        int lessonsCompleted = trackProgressRepository.findByIdUserId(userId).stream()
                .mapToInt(TrackProgress::getCompletedLessons)
                .sum();
        double avgProgress = trackProgressRepository.findByIdUserId(userId).stream()
                .mapToInt(TrackProgress::getProgressPct)
                .average()
                .orElse(0);
        return StudentDashboardResponse.builder()
                .totalEnrollments(enrollments.size())
                .inProgress((int) inProgress)
                .completed((int) completed)
                .totalLessonsCompleted(lessonsCompleted)
                .averageProgress(Math.round(avgProgress * 10.0) / 10.0)
                .build();
    }

    public EnrollmentCheckResponse checkEnrollment(Long userId, String trackId) {
        return enrollmentRepository.findByUserIdAndTrackId(userId, trackId)
                .map(e -> EnrollmentCheckResponse.builder()
                        .enrolled(true)
                        .status(e.getStatus())
                        .enrollmentId(e.getId())
                        .build())
                .orElse(EnrollmentCheckResponse.builder().enrolled(false).build());
    }

    @Transactional
    public void cancelEnrollment(Long enrollmentId, String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        enrollment.setStatus("CANCELLED");
        enrollmentRepository.save(enrollment);
    }

    private void requireEnrollment(Long userId, String trackId) {
        if (!enrollmentRepository.existsByUserIdAndTrackId(userId, trackId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enrolled in this track");
        }
    }

    private void initTrackProgress(Long userId, String trackId) {
        int total = CatalogMetadata.totalLessonsForTrack(trackId);
        trackProgressRepository.save(TrackProgress.builder()
                .id(new TrackProgressId(userId, trackId))
                .totalLessons(total)
                .completedLessons(0)
                .progressPct(0)
                .build());
    }

    private TrackProgress recalculateTrackProgress(Long userId, String trackId, Long lastLessonId) {
        int total = CatalogMetadata.totalLessonsForTrack(trackId);
        long completed = lessonProgressRepository.countByUserIdAndTrackIdAndCompletedTrue(userId, trackId);
        int pct = total > 0 ? (int) Math.min(100, (completed * 100) / total) : 0;
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, trackId)
                .orElse(TrackProgress.builder().id(new TrackProgressId(userId, trackId)).build());
        tp.setTotalLessons(total);
        tp.setCompletedLessons((int) completed);
        tp.setProgressPct(pct);
        tp.setLastLessonId(lastLessonId);
        return trackProgressRepository.save(tp);
    }

    private MyCourseResponse toMyCourse(Enrollment enrollment) {
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(
                enrollment.getUserId(), enrollment.getTrackId()).orElse(null);
        var meta = CatalogMetadata.forTrack(enrollment.getTrackId());
        int progress = tp != null ? tp.getProgressPct() : 0;
        String status = "COMPLETED".equals(enrollment.getStatus()) ? "completed"
                : progress > 0 ? "in-progress" : "not-started";
        return MyCourseResponse.builder()
                .id(enrollment.getId())
                .trackId(enrollment.getTrackId())
                .courseId(enrollment.getCourseId())
                .title(meta.map(m -> m.title()).orElse("Course"))
                .image(meta.map(m -> m.image()).orElse(""))
                .progress(progress)
                .status(status)
                .totalLessons(tp != null ? tp.getTotalLessons() : meta.map(m -> m.totalLessons()).orElse(0))
                .completedLessons(tp != null ? tp.getCompletedLessons() : 0)
                .badge(meta.map(m -> m.badge()).orElse(""))
                .instructor(meta.map(m -> m.instructor()).orElse(""))
                .rating(meta.map(m -> m.rating()).orElse("0"))
                .duration(meta.map(m -> m.duration()).orElse(""))
                .modules(meta.map(m -> m.modules()).orElse(""))
                .description(meta.map(m -> m.description()).orElse(""))
                .build();
    }

    private EnrollmentDetailResponse toDetail(Enrollment enrollment) {
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(
                enrollment.getUserId(), enrollment.getTrackId()).orElse(null);
        return EnrollmentDetailResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .trackId(enrollment.getTrackId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .progress(tp != null ? tp.getProgressPct() : 0)
                .completedLessons(tp != null ? tp.getCompletedLessons() : 0)
                .totalLessons(tp != null ? tp.getTotalLessons() : CatalogMetadata.totalLessonsForTrack(enrollment.getTrackId()))
                .lastLessonId(tp != null ? tp.getLastLessonId() : null)
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
