package com.lms.catalog.seed;

import com.lms.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class CatalogCourseCleanup implements CommandLineRunner {

    private final CatalogService catalogService;

    @Override
    public void run(String... args) {
        try {
            catalogService.purgePlatformSeedCourses();
            catalogService.purgeIneligibleMentorCourses();
            log.info("Catalog cleanup complete");
        } catch (Exception ex) {
            log.warn("Catalog cleanup skipped: {}", ex.getMessage());
        }
    }
}
