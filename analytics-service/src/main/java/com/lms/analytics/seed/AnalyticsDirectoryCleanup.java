package com.lms.analytics.seed;

import com.lms.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Clears analytics metrics on startup — OFF by default. */
@Component
@ConditionalOnProperty(name = "lms.directory.cleanup.enabled", havingValue = "true")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AnalyticsDirectoryCleanup implements CommandLineRunner {

    private final DailyMetricRepository dailyMetricRepository;
    private final MentorMetricRepository mentorMetricRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final StudentActivityRepository studentActivityRepository;

    @Override
    @Transactional
    public void run(String... args) {
        long daily = dailyMetricRepository.count();
        long mentor = mentorMetricRepository.count();
        long course = courseMetricRepository.count();
        long activity = studentActivityRepository.count();
        if (daily + mentor + course + activity == 0) {
            return;
        }
        dailyMetricRepository.deleteAll();
        mentorMetricRepository.deleteAll();
        courseMetricRepository.deleteAll();
        studentActivityRepository.deleteAll();
        log.info("Analytics cleanup cleared seeded metrics (daily={}, mentor={}, course={}, activity={})",
                daily, mentor, course, activity);
    }
}
