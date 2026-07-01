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
        seedStudentNotifications();
        seedMentorNotifications();
        seedAdminNotifications();
    }

    private void seedPreferences() {
        for (long userId = 1; userId <= 3; userId++) {
            preferenceRepository.save(NotificationPreference.builder().userId(userId).build());
        }
    }

    private void seedStudentNotifications() {
        long userId = 3L;
        seed(userId, "mentorship", "New reply from your mentor",
                "Jane Doe replied to your question in \"React State Architecture\".",
                "/student/courses", "Reply to mentor", null, false, hoursAgo(2));
        seed(userId, "achievement", "Certificate unlocked!",
                "You completed Cloud Architecture Patterns. Your certificate is ready to download.",
                "/student/certificates", "View certificate", null, false, daysAgo(1));
        seed(userId, "course", "Course content updated",
                "3 new lessons added to \"Enterprise React Systems\" — hooks, context, and performance.",
                "/student/courses", "Continue course", null, false, daysAgo(2));
        seed(userId, "assignment", "Assignment due tomorrow",
                "Submit your Cloud Architecture diagram before 11:59 PM PT.",
                "/student/assignments", "Open assignment", null, true, daysAgo(3));
        seed(userId, "live", "Live class starting soon",
                "React Masterclass begins in 30 minutes. Join the session from your dashboard.",
                "/student/notifications", "Join live class", null, true, daysAgo(4));
        seed(userId, "quiz", "Quiz results available",
                "You scored 92% on the Cloud Architecture Quiz. Review your answers and feedback.",
                "/student/quiz", "View results", null, true, daysAgo(5));
        seed(userId, "system", "Weekly learning summary",
                "You studied 7 hours this week — 70% of your goal. Keep the streak going!",
                "/student/courses", "View progress", null, true, daysAgo(7));
        seed(userId, "mentorship", "Mentor feedback on your project",
                "Dr. Arjan Singh left detailed feedback on your \"AWS VPC Design\" submission.",
                "/student/courses", "Read feedback", null, false, minutesAgo(35));
        seed(userId, "course", "New module unlocked",
                "You unlocked Module 4 in \"Azure Generative AI\" — explore prompt engineering and RAG pipelines.",
                "/learn/ai", "Start module", null, false, hoursAgo(5));
        seed(userId, "achievement", "7-day learning streak!",
                "You've studied every day this week. Earn the Consistency Champion badge at 14 days.",
                "/student/profile", "View badges", null, false, daysAgo(1));
    }

    private void seedMentorNotifications() {
        long userId = 2L;
        seed(userId, "qna", "New Q&A question",
                "A student asked about Redux vs Zustand in \"Advanced State Management\" — Lesson 3.",
                "/mentor/notifications", "Reply now", null, false, hoursAgo(2));
        seed(userId, "review", "5-star review received",
                "Sarah M. left a 5-star review on \"Cloud Architecture Patterns\".",
                "/mentor/analytics", "View review", null, false, hoursAgo(5));
        seed(userId, "enrollment", "New student enrolled",
                "Alex Chen enrolled in \"Advanced State Management\".",
                "/mentor/students", "View student", null, false, hoursAgo(8));
        seed(userId, "payout", "Payout processed",
                "Your monthly payout of $4,250 has been processed and will arrive in 1–2 business days.",
                "/mentor/analytics", "View details", null, true, daysAgo(1));
        seed(userId, "approval", "Course approved & live",
                "\"Rust for Frontend Devs\" passed QA review and is now live on the marketplace.",
                "/mentor/lessons", "View course", null, true, daysAgo(2));
        seed(userId, "trending", "Course is trending",
                "\"Cloud Architecture Patterns\" is in the top 5 today — 84 new enrollments in 24 hours.",
                "/mentor/analytics", "See analytics", null, true, daysAgo(3));
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
                "Cloud Nexus v1.2.4 deployed to production with zero downtime.",
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
