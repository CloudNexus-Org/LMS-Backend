package com.lms.notification.seed;

import com.lms.notification.model.Notification;
import com.lms.notification.model.NotificationPreference;
import com.lms.notification.repository.NotificationPreferenceRepository;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (notificationRepository.count() > 0) {
            return;
        }

        seedPreferences();
        seedAdminNotifications();
    }

    private void seedPreferences() {
        preferenceRepository.save(NotificationPreference.builder().userId(1L).build());
    }

    private void seedAdminNotifications() {
        long userId = 1L;
        seed(userId, "alert", "High server load detected",
                "Database CPU utilization hit 85% in us-east-1. Auto-scaling has been initiated.",
                "/admin/reports", "View metrics", "critical", false, minutesAgo(10));
        seed(userId, "approval", "14 courses awaiting QA review",
                "Mentor submissions need quality assurance before publishing.",
                "/admin/approvals", "Review now", "high", false, hoursAgo(1));
        seed(userId, "user", "New mentor application",
                "David Kim applied to mentor Backend Engineering & Systems Design.",
                "/admin/users", "View application", "normal", false, hoursAgo(3));
        seed(userId, "payout", "Monthly payouts pending authorization",
                "$42,500 in mentor payouts across 18 mentors awaits your authorization.",
                "/admin/revenue", "Authorize payouts", "high", true, hoursAgo(5));
        seed(userId, "security", "Unusual login activity detected",
                "142 failed login attempts from 3 IPs in the last 2 hours. Auto-block triggered.",
                "/admin/settings", "View security logs", "critical", true, daysAgo(1));
        seed(userId, "system", "Automated database backup complete",
                "Daily snapshot of production databases completed. 12.4 GB stored to S3.",
                "/admin/reports", "View backup log", "normal", true, hoursAgo(12));
        seed(userId, "deployment", "Platform deployment successful",
                "Realm v1.2.4 deployed to production with zero downtime.",
                null, null, "normal", true, daysAgo(2));
    }

    private void seed(
            Long userId,
            String type,
            String title,
            String message,
            String link,
            String actionLabel,
            String priority,
            boolean read,
            Instant createdAt) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .actionLabel(actionLabel)
                .priority(priority)
                .read(read)
                .createdAt(createdAt)
                .build());
    }

    private Instant minutesAgo(long minutes) {
        return Instant.now().minus(minutes, ChronoUnit.MINUTES);
    }

    private Instant hoursAgo(long hours) {
        return Instant.now().minus(hours, ChronoUnit.HOURS);
    }

    private Instant daysAgo(long days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }
}
