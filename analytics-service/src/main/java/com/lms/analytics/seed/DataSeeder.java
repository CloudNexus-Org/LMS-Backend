package com.lms.analytics.seed;

import com.lms.analytics.model.*;
import com.lms.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DailyMetricRepository dailyMetricRepository;
    private final MentorMetricRepository mentorMetricRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final StudentActivityRepository studentActivityRepository;

    private static final Long SEED_USER_ID = 3L;
    private static final Long MENTOR_ID = 2L;

    @Override
    @Transactional
    public void run(String... args) {
        if (dailyMetricRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<DailyMetric> daily = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            daily.add(DailyMetric.builder()
                    .date(date)
                    .totalUsers(12000 + (30 - i) * 15)
                    .newUsers(20 + (i % 7) * 5)
                    .totalRevenue(BigDecimal.valueOf(3500 + (30 - i) * 120L))
                    .enrollments(15 + (i % 5) * 3)
                    .completions(8 + (i % 4) * 2)
                    .build());
        }
        dailyMetricRepository.saveAll(daily);

        for (int i = 6; i >= 0; i--) {
            mentorMetricRepository.save(MentorMetric.builder()
                    .id(new MentorMetricId(MENTOR_ID, today.minusDays(i)))
                    .revenue(BigDecimal.valueOf(600 + i * 80L))
                    .newStudents(10 + i * 2)
                    .activeStudents(1248 - i * 5)
                    .build());
        }

        for (long courseId = 1; courseId <= 4; courseId++) {
            for (int i = 6; i >= 0; i--) {
                courseMetricRepository.save(CourseMetric.builder()
                        .id(new CourseMetricId(courseId, today.minusDays(i)))
                        .views(100 + (int) courseId * 20 + i * 10)
                        .enrollments(12 + (int) courseId * 3 + i)
                        .completions(8 + (int) courseId * 2)
                        .avgRating(4.5 + courseId * 0.1)
                        .build());
            }
        }

        for (int i = 6; i >= 0; i--) {
            studentActivityRepository.save(StudentActivity.builder()
                    .id(new StudentActivityId(SEED_USER_ID, today.minusDays(i)))
                    .lessonsCompleted(3 + (i % 4))
                    .minutesLearned(45 + i * 15)
                    .quizzesTaken(i % 3)
                    .build());
        }
    }
}
