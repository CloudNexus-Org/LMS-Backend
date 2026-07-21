package com.lms.content.seed;

import com.lms.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Purges pending courses for inactive mentors — OFF by default. */
@Component
@ConditionalOnProperty(name = "lms.directory.cleanup.enabled", havingValue = "true")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ContentDirectoryCleanup implements CommandLineRunner {

    private final ContentService contentService;

    @Override
    public void run(String... args) {
        try {
            contentService.purgeIneligibleSubmissions();
            log.info("Content directory cleanup complete");
        } catch (Exception ex) {
            log.warn("Content directory cleanup skipped: {}", ex.getMessage());
        }
    }
}
