package com.lms.enrollment.seed;

import com.lms.enrollment.model.Enrollment;
import com.lms.enrollment.model.TrackProgressId;
import com.lms.enrollment.repository.EnrollmentRepository;
import com.lms.enrollment.repository.LessonProgressRepository;
import com.lms.enrollment.repository.TrackProgressRepository;
import com.lms.enrollment.service.CatalogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class EnrollmentDirectoryCleanup implements CommandLineRunner {

    private static final Set<String> DEMO_TRACK_IDS = Set.of(
            "cloud", "ai", "fullstack", "devops", "data", "backend"
    );

    private final EnrollmentRepository enrollmentRepository;
    private final TrackProgressRepository trackProgressRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CatalogClient catalogClient;

    @Override
    @Transactional
    public void run(String... args) {
        int removed = 0;
        Set<String> affectedTracks = new HashSet<>();

        for (Enrollment enrollment : enrollmentRepository.findAll()) {
            if (shouldRemove(enrollment)) {
                affectedTracks.add(enrollment.getTrackId());
                lessonProgressRepository.deleteByUserIdAndTrackId(
                        enrollment.getUserId(), enrollment.getTrackId());
                enrollmentRepository.delete(enrollment);
                removed++;
            }
        }

        for (String trackId : DEMO_TRACK_IDS) {
            trackProgressRepository.findAll().stream()
                    .filter(tp -> trackId.equals(tp.getId().getTrackId()))
                    .forEach(trackProgressRepository::delete);
        }

        for (String trackId : affectedTracks) {
            if (DEMO_TRACK_IDS.contains(trackId)) {
                trackProgressRepository.findAll().stream()
                        .filter(tp -> trackId.equals(tp.getId().getTrackId()))
                        .forEach(trackProgressRepository::delete);
            }
        }

        if (removed > 0) {
            log.info("Enrollment cleanup: removed {} seed/invalid enrollments", removed);
        }
    }

    private boolean shouldRemove(Enrollment enrollment) {
        Long courseId = enrollment.getCourseId();
        if (courseId != null && courseId < 10) {
            return true;
        }
        if (enrollment.getTrackId() != null && DEMO_TRACK_IDS.contains(enrollment.getTrackId())) {
            return true;
        }
        if (courseId == null) {
            return true;
        }
        return catalogClient.findCourse(courseId).isEmpty();
    }
}
