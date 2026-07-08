package com.lms.admin.seed;

import com.lms.admin.model.*;
import com.lms.admin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseApprovalRepository courseApprovalRepository;
    private final PlatformSettingRepository platformSettingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        clearLegacySeedApprovals();
        if (platformSettingRepository.count() == 0) {
            seedSettings();
        }
    }

    private void clearLegacySeedApprovals() {
        courseApprovalRepository.deleteAllById(List.of("C-8291", "C-8292", "C-8290", "C-8289"));
    }

    private void seedSettings() {
        Instant now = Instant.now();
        platformSettingRepository.saveAll(List.of(
                setting("platform.name", "Cloud Nexus", now),
                setting("platform.gst_rate", "0.18", now),
                setting("platform.commission_pct", "0.30", now),
                setting("platform.currency", "INR", now),
                setting("platform.support_email", "support@cloudnexus.com", now)
        ));
    }

    private static PlatformSetting setting(String key, String value, Instant updatedAt) {
        return PlatformSetting.builder()
                .key(key)
                .value(value)
                .updatedAt(updatedAt)
                .updatedBy(1L)
                .build();
    }
}
