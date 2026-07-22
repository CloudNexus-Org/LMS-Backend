package com.lms.enrollment.service;

import com.lms.enrollment.dto.*;
import com.lms.enrollment.event.EnrollmentEventProducer;
import com.lms.enrollment.model.*;
import com.lms.enrollment.repository.EnrollmentRepository;
import com.lms.enrollment.repository.LessonProgressRepository;
import com.lms.enrollment.repository.QuizAttemptRepository;
import com.lms.enrollment.repository.TrackProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TrackProgressRepository trackProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EnrollmentEventProducer eventProducer;
    private final CatalogClient catalogClient;
    private final UserClient userClient;

    @Transactional
    public EnrollmentDetailResponse enroll(Long userId, EnrollRequest request) {
        if (request.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseId is required");
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this course");
        }
        String trackId = catalogClient.resolveTrackId(request.getCourseId(), request.getTrackId());
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userId(userId)
                .trackId(trackId)
                .courseId(request.getCourseId())
                .status("ACTIVE")
                .build());
        initTrackProgressIfNeeded(userId, trackId, request.getCourseId());
        publishEnrollmentCreatedEvent(userId, trackId, enrollment.getId(), request.getCourseId());
        return toDetail(enrollment);
    }

    @Transactional
    public void enrollFromPayment(Long userId, String trackId, Long courseId) {
        if (courseId == null) {
            log.warn("payment.success missing courseId for user {}", userId);
            return;
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return;
        }
        String resolvedTrackId = catalogClient.resolveTrackId(courseId, trackId);
        Enrollment enrollment = enrollmentRepository.save(Enrollment.builder()
                .userId(userId)
                .trackId(resolvedTrackId)
                .courseId(courseId)
                .status("ACTIVE")
                .build());
        initTrackProgressIfNeeded(userId, resolvedTrackId, courseId);
        publishEnrollmentCreatedEvent(userId, resolvedTrackId, enrollment.getId(), courseId);
    }

    private void publishEnrollmentCreatedEvent(Long userId, String trackId, Long enrollmentId, Long courseId) {
        var course = catalogClient.findCourse(courseId);
        Long mentorId = course.map(CatalogClient.CourseSnapshot::mentorId).orElse(null);
        String courseTitle = course.map(CatalogClient.CourseSnapshot::title).filter(t -> !t.isBlank()).orElse(null);
        String studentName = userClient.displayName(userId).orElse("Student #" + userId);
        eventProducer.publishEnrollmentCreated(
                userId,
                trackId,
                enrollmentId,
                courseId,
                mentorId,
                courseTitle,
                studentName,
                Instant.now()
        );
    }

    public List<MyCourseResponse> myEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .filter(e -> !"CANCELLED".equalsIgnoreCase(e.getStatus()))
                .map(this::toMyCourse)
                .toList();
    }

    public EnrollmentDetailResponse myEnrollmentByTrack(Long userId, String trackId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndTrackId(userId, trackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        return toDetail(enrollment);
    }

    public CourseProgressResponse courseProgress(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No enrollment for course"));
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, enrollment.getTrackId())
                .orElse(null);
        List<Long> completedIds = lessonProgressRepository
                .findByUserIdAndTrackIdAndCompletedTrue(userId, enrollment.getTrackId()).stream()
                .map(LessonProgress::getLessonId)
                .toList();
        List<Long> quizPassedIds = lessonProgressRepository
                .findByUserIdAndTrackId(userId, enrollment.getTrackId()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getQuizPassed()))
                .map(LessonProgress::getLessonId)
                .toList();
        return CourseProgressResponse.builder()
                .courseId(courseId)
                .trackId(enrollment.getTrackId())
                .progress(tp != null ? tp.getProgressPct() : 0)
                .completedLessons(tp != null ? tp.getCompletedLessons() : 0)
                .totalLessons(tp != null ? tp.getTotalLessons() : CatalogMetadata.totalLessonsForTrack(enrollment.getTrackId()))
                .completedLessonIds(completedIds)
                .quizPassedLessonIds(quizPassedIds)
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
        return toTrackProgressResponse(userId, trackId, tp);
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
        if (Boolean.TRUE.equals(request.getRequireQuizPass()) && !Boolean.TRUE.equals(progress.getQuizPassed())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pass the lesson quiz before marking this lesson complete");
        }
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
        return toTrackProgressResponse(userId, request.getTrackId(), tp);
    }

    @Transactional
    public QuizAttemptResponse submitQuizAttempt(Long userId, Long lessonId, QuizAttemptRequest request) {
        if (request.getTrackId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        requireEnrollment(userId, request.getTrackId());
        int score = request.getScore() != null ? request.getScore() : 0;
        int total = request.getTotalQuestions() != null ? Math.max(1, request.getTotalQuestions()) : 1;
        int passing = request.getPassingScore() != null ? request.getPassingScore() : 70;
        boolean passed = request.getPassed() != null
                ? request.getPassed()
                : (score * 100 / total) >= passing;

        String answersJson = null;
        if (request.getAnswers() != null) {
            try {
                answersJson = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(request.getAnswers());
            } catch (Exception ignored) {
                answersJson = null;
            }
        }

        QuizAttempt attempt = quizAttemptRepository.save(QuizAttempt.builder()
                .userId(userId)
                .lessonId(lessonId)
                .trackId(request.getTrackId())
                .score(score)
                .totalQuestions(total)
                .passingScore(passing)
                .passed(passed)
                .answersJson(answersJson)
                .build());

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .trackId(request.getTrackId())
                        .build());
        if (passed) {
            progress.setQuizPassed(true);
            progress.setQuizPassedAt(Instant.now());
        }
        lessonProgressRepository.save(progress);

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .lessonId(lessonId)
                .trackId(request.getTrackId())
                .score(score)
                .totalQuestions(total)
                .passingScore(passing)
                .passed(passed)
                .attemptedAt(attempt.getAttemptedAt())
                .build();
    }

    public List<QuizAttemptResponse> quizAttemptsForLesson(Long userId, Long lessonId) {
        return quizAttemptRepository.findByUserIdAndLessonIdOrderByAttemptedAtDesc(userId, lessonId).stream()
                .map(a -> QuizAttemptResponse.builder()
                        .id(a.getId())
                        .lessonId(a.getLessonId())
                        .trackId(a.getTrackId())
                        .score(a.getScore())
                        .totalQuestions(a.getTotalQuestions())
                        .passingScore(a.getPassingScore())
                        .passed(a.getPassed())
                        .attemptedAt(a.getAttemptedAt())
                        .build())
                .toList();
    }

    public Map<String, Object> lessonUnlockStatus(Long userId, Long lessonId, String trackId) {
        requireEnrollment(userId, trackId);
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId).orElse(null);
        boolean completed = progress != null && Boolean.TRUE.equals(progress.getCompleted());
        boolean quizPassed = progress != null && Boolean.TRUE.equals(progress.getQuizPassed());
        return Map.of(
                "lessonId", lessonId,
                "trackId", trackId,
                "completed", completed,
                "quizPassed", quizPassed
        );
    }

    @Transactional
    public TrackProgressResponse finishTrack(Long userId, String trackId) {
        requireEnrollment(userId, trackId);
        TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, trackId)
                .orElseGet(() -> TrackProgress.builder()
                        .id(new TrackProgressId(userId, trackId))
                        .totalLessons(CatalogMetadata.totalLessonsForTrack(trackId))
                        .build());
        int total = tp.getTotalLessons() != null && tp.getTotalLessons() > 0
                ? tp.getTotalLessons()
                : CatalogMetadata.totalLessonsForTrack(trackId);
        tp.setTotalLessons(total);
        tp.setCompletedLessons(total);
        tp.setProgressPct(100);
        trackProgressRepository.save(tp);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndTrackId(userId, trackId).orElseThrow();
        if (!"COMPLETED".equals(enrollment.getStatus())) {
            enrollment.setStatus("COMPLETED");
            enrollmentRepository.save(enrollment);
            eventProducer.publishTrackCompleted(userId, trackId);
        }

        return toTrackProgressResponse(userId, trackId, tp);
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

    public long activeEnrollmentCount(Long courseId) {
        if (courseId == null) return 0;
        return enrollmentRepository.countByCourseIdAndStatusNotIgnoreCase(courseId, "CANCELLED");
    }

    public Map<Long, Long> activeEnrollmentCounts(List<Long> courseIds) {
        Map<Long, Long> counts = new java.util.LinkedHashMap<>();
        if (courseIds == null) return counts;
        for (Long courseId : courseIds) {
            if (courseId == null) continue;
            counts.put(courseId, activeEnrollmentCount(courseId));
        }
        return counts;
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

    private int resolveTotalLessons(Long userId, String trackId, Long courseId) {
        if (courseId != null) {
            int fromCatalog = catalogClient.findCourse(courseId)
                    .map(CatalogClient.CourseSnapshot::totalLessons)
                    .orElse(0);
            if (fromCatalog > 0) {
                return fromCatalog;
            }
        }
        if (userId != null && trackId != null) {
            TrackProgress tp = trackProgressRepository.findByIdUserIdAndIdTrackId(userId, trackId).orElse(null);
            if (tp != null && tp.getTotalLessons() != null && tp.getTotalLessons() > 0) {
                return tp.getTotalLessons();
            }
        }
        return CatalogMetadata.totalLessonsForCourse(courseId);
    }

    private static int computeProgressPct(int completed, int total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.min(100, (completed * 100) / total);
    }

    private void initTrackProgressIfNeeded(Long userId, String trackId, Long courseId) {
        if (trackProgressRepository.findByIdUserIdAndIdTrackId(userId, trackId).isPresent()) {
            return;
        }
        int total = resolveTotalLessons(userId, trackId, courseId);
        trackProgressRepository.save(TrackProgress.builder()
                .id(new TrackProgressId(userId, trackId))
                .totalLessons(total)
                .completedLessons(0)
                .progressPct(0)
                .build());
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
        Long courseId = enrollmentRepository.findByUserIdAndTrackId(userId, trackId)
                .map(Enrollment::getCourseId)
                .orElse(null);
        int total = resolveTotalLessons(userId, trackId, courseId);
        long completed = lessonProgressRepository.countByUserIdAndTrackIdAndCompletedTrue(userId, trackId);
        int pct = computeProgressPct((int) completed, total);
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
        var catalogCourse = catalogClient.findCourse(enrollment.getCourseId());
        var trackMeta = CatalogMetadata.forTrack(enrollment.getTrackId());

        int totalLessons = catalogCourse.map(CatalogClient.CourseSnapshot::totalLessons)
                .filter(n -> n > 0)
                .orElseGet(() -> resolveTotalLessons(
                        enrollment.getUserId(), enrollment.getTrackId(), enrollment.getCourseId()));
        int completedLessons = tp != null ? tp.getCompletedLessons() : 0;
        int progress = computeProgressPct(completedLessons, totalLessons);
        if (progress == 0 && tp != null && tp.getProgressPct() != null && tp.getProgressPct() > 0) {
            progress = tp.getProgressPct();
        }
        String status = "COMPLETED".equals(enrollment.getStatus()) ? "completed"
                : progress >= 100 ? "completed"
                : completedLessons > 0 || progress > 0 ? "in-progress" : "not-started";

        String title = catalogCourse.map(CatalogClient.CourseSnapshot::title)
                .orElseGet(() -> trackMeta.map(m -> m.title()).orElse("Course"));
        String image = catalogCourse.map(CatalogClient.CourseSnapshot::image)
                .orElseGet(() -> trackMeta.map(m -> m.image()).orElse(""));
        String badge = catalogCourse.map(CatalogClient.CourseSnapshot::badge)
                .orElseGet(() -> trackMeta.map(m -> m.badge()).orElse(""));
        String instructor = catalogCourse.map(CatalogClient.CourseSnapshot::instructor)
                .orElseGet(() -> trackMeta.map(m -> m.instructor()).orElse(""));
        String rating = catalogCourse.map(CatalogClient.CourseSnapshot::rating)
                .orElseGet(() -> trackMeta.map(m -> m.rating()).orElse("0"));
        String duration = catalogCourse.map(CatalogClient.CourseSnapshot::duration)
                .orElseGet(() -> trackMeta.map(m -> m.duration()).orElse(""));
        String modules = catalogCourse.map(CatalogClient.CourseSnapshot::modules)
                .orElseGet(() -> trackMeta.map(m -> m.modules()).orElse(""));
        String description = catalogCourse.map(CatalogClient.CourseSnapshot::description)
                .orElseGet(() -> trackMeta.map(m -> m.description()).orElse(""));

        return MyCourseResponse.builder()
                .id(enrollment.getId())
                .trackId(enrollment.getTrackId())
                .courseId(enrollment.getCourseId())
                .title(title)
                .image(image)
                .progress(progress)
                .status(status)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .badge(badge)
                .instructor(instructor)
                .rating(rating)
                .duration(duration)
                .modules(modules)
                .description(description)
                .build();
    }

    private TrackProgressResponse toTrackProgressResponse(Long userId, String trackId, TrackProgress tp) {
        List<Long> completedIds = lessonProgressRepository
                .findByUserIdAndTrackIdAndCompletedTrue(userId, trackId).stream()
                .map(LessonProgress::getLessonId)
                .toList();
        List<Long> quizPassedIds = lessonProgressRepository
                .findByUserIdAndTrackId(userId, trackId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getQuizPassed()))
                .map(LessonProgress::getLessonId)
                .toList();
        return TrackProgressResponse.builder()
                .trackId(trackId)
                .progress(tp != null ? tp.getProgressPct() : 0)
                .completedLessons(tp != null ? tp.getCompletedLessons() : completedIds.size())
                .totalLessons(tp != null ? tp.getTotalLessons() : CatalogMetadata.totalLessonsForTrack(trackId))
                .lastLessonId(tp != null ? tp.getLastLessonId() : null)
                .completedLessonIds(completedIds)
                .quizPassedLessonIds(quizPassedIds)
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
