package com.lms.enrollment.seed;

import com.lms.enrollment.model.*;
import com.lms.enrollment.repository.EnrollmentRepository;
import com.lms.enrollment.repository.LessonProgressRepository;
import com.lms.enrollment.repository.TrackProgressRepository;
import com.lms.enrollment.service.CatalogMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EnrollmentRepository enrollmentRepository;
    private final TrackProgressRepository trackProgressRepository;
    private final LessonProgressRepository lessonProgressRepository;

    private static final Long SEED_USER_ID = 3L;

    @Override
    @Transactional
    public void run(String... args) {
        if (enrollmentRepository.count() > 0 && lessonProgressRepository.count() > 0) {
            return;
        }
        if (enrollmentRepository.count() > 0) {
            lessonProgressRepository.deleteAll();
            trackProgressRepository.deleteAll();
            enrollmentRepository.deleteAll();
        }

        Enrollment cloud = enrollmentRepository.save(Enrollment.builder()
                .userId(SEED_USER_ID)
                .trackId("cloud")
                .courseId(1L)
                .status("ACTIVE")
                .build());

        Enrollment ai = enrollmentRepository.save(Enrollment.builder()
                .userId(SEED_USER_ID)
                .trackId("ai")
                .courseId(2L)
                .status("COMPLETED")
                .build());

        trackProgressRepository.save(TrackProgress.builder()
                .id(new TrackProgressId(SEED_USER_ID, "cloud"))
                .completedLessons(27)
                .totalLessons(42)
                .progressPct(65)
                .lastLessonId(1027L)
                .build());

        trackProgressRepository.save(TrackProgress.builder()
                .id(new TrackProgressId(SEED_USER_ID, "ai"))
                .completedLessons(32)
                .totalLessons(32)
                .progressPct(100)
                .lastLessonId(2032L)
                .build());

        for (long lessonId = 1001; lessonId <= 1027; lessonId++) {
            lessonProgressRepository.save(LessonProgress.builder()
                    .userId(SEED_USER_ID)
                    .lessonId(lessonId)
                    .trackId("cloud")
                    .completed(true)
                    .completedAt(Instant.now())
                    .watchDurationSec(900)
                    .build());
        }

        for (long lessonId = 2001; lessonId <= 2032; lessonId++) {
            lessonProgressRepository.save(LessonProgress.builder()
                    .userId(SEED_USER_ID)
                    .lessonId(lessonId)
                    .trackId("ai")
                    .completed(true)
                    .completedAt(Instant.now())
                    .watchDurationSec(1200)
                    .build());
        }
    }
}
