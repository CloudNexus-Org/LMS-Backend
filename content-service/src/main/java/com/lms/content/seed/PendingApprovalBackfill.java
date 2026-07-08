package com.lms.content.seed;

import com.lms.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class PendingApprovalBackfill implements CommandLineRunner {

    private final ContentService contentService;

    @Override
    public void run(String... args) {
        try {
            contentService.backfillPendingApprovals();
            log.info("Pending course approval backfill complete");
        } catch (Exception ex) {
            log.warn("Pending approval backfill skipped: {}", ex.getMessage());
        }
    }
}
