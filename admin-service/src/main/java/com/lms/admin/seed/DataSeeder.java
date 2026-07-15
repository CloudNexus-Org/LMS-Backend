package com.lms.admin.seed;

import com.lms.admin.model.*;
import com.lms.admin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseApprovalRepository courseApprovalRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final MentorPayoutRepository mentorPayoutRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseApprovalRepository.count() > 0) {
            return;
        }
        seedApprovals();
        seedSettings();
        seedTransactions();
        seedPayouts();
    }

    private void seedApprovals() {
        Instant now = Instant.now();
        courseApprovalRepository.saveAll(List.of(
                approval("C-8291", "Advanced Next.js 15 Patterns", "Sarah Chen", "SC",
                        "Frontend Engineering", "2 hours ago", 12, 48, "4h 15m", 4.7,
                        "from-blue-500 to-cyan-400", "Pending", "high",
                        "Deep dive into Next.js 15 server components, caching strategies, and app router patterns.",
                        201L, now.minus(2, ChronoUnit.HOURS)),
                approval("C-8292", "Go Microservices Architecture", "David Kim", "DK",
                        "Backend & Systems", "1 day ago", 8, 32, "6h 30m", 4.9,
                        "from-emerald-500 to-teal-400", "Pending", "normal",
                        "Build production-ready microservices with Go, gRPC, Kafka, and Kubernetes orchestration.",
                        202L, now.minus(1, ChronoUnit.DAYS)),
                approval("C-8290", "LangChain & LLM Engineering", "Priya Nair", "PN",
                        "Data & AI", "3 days ago", 10, 40, "5h 45m", 4.8,
                        "from-violet-500 to-fuchsia-400", "Pending", "normal",
                        "Production LLM apps using LangChain, vector databases, RAG pipelines, and fine-tuning.",
                        203L, now.minus(3, ChronoUnit.DAYS)),
                approval("C-8289", "Rust Systems Programming", "Liam Carter", "LC",
                        "Systems Programming", "5 days ago", 14, 56, "8h 00m", 5.0,
                        "from-orange-500 to-red-400", "Approved", "normal",
                        "Comprehensive Rust course covering ownership, lifetimes, async, and systems-level programming.",
                        204L, now.minus(5, ChronoUnit.DAYS), now.minus(4, ChronoUnit.DAYS), 1L, null)
        ));
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

    private void seedTransactions() {
        Instant now = Instant.now();
        financialTransactionRepository.saveAll(List.of(
                transaction("TX-9281", "Course Sale", new BigDecimal("89.99"), new BigDecimal("26.99"),
                        301L, "TX-9281", "Alex Chen", "Advanced State Management",
                        "10 mins ago", now.minus(10, ChronoUnit.MINUTES)),
                transaction("TX-9280", "Course Sale", new BigDecimal("129.99"), new BigDecimal("38.99"),
                        302L, "TX-9280", "Sarah Miller", "Cloud Architecture Patterns",
                        "1 hour ago", now.minus(1, ChronoUnit.HOURS)),
                transaction("PO-4091", "Mentor Payout", new BigDecimal("-4250"), null,
                        203L, "PO-4091", "Priya Nair", "Monthly payout",
                        "Yesterday", now.minus(1, ChronoUnit.DAYS)),
                transaction("TX-9279", "Refund", new BigDecimal("-89.99"), new BigDecimal("-26.99"),
                        303L, "TX-9279", "James Wilson", "React Performance Patterns",
                        "Yesterday", now.minus(1, ChronoUnit.DAYS)),
                transaction("TX-9278", "Course Sale", new BigDecimal("79.99"), new BigDecimal("23.99"),
                        304L, "TX-9278", "Emily Davis", "System Design Fundamentals",
                        "2 days ago", now.minus(2, ChronoUnit.DAYS)),
                transaction("PO-4090", "Mentor Payout", new BigDecimal("-3180"), null,
                        201L, "PO-4090", "Sarah Chen", "Monthly payout",
                        "3 days ago", now.minus(3, ChronoUnit.DAYS))
        ));
    }

    private void seedPayouts() {
        Instant now = Instant.now();
        mentorPayoutRepository.saveAll(List.of(
                MentorPayout.builder()
                        .id("PO-4091")
                        .mentorId(203L)
                        .mentorName("Priya Nair")
                        .amount(new BigDecimal("4250"))
                        .period("Monthly payout")
                        .status("Processed")
                        .processedAt(now.minus(1, ChronoUnit.DAYS))
                        .build(),
                MentorPayout.builder()
                        .id("PO-4090")
                        .mentorId(201L)
                        .mentorName("Sarah Chen")
                        .amount(new BigDecimal("3180"))
                        .period("Monthly payout")
                        .status("Processed")
                        .processedAt(now.minus(3, ChronoUnit.DAYS))
                        .build()
        ));
    }

    private static CourseApproval approval(String courseId, String title, String mentor, String mentorAvatar,
                                           String category, String submitted, int modules, int lessons,
                                           String duration, double previewRating, String thumbnail,
                                           String status, String priority, String description,
                                           Long mentorId, Instant submittedAt) {
        return approval(courseId, title, mentor, mentorAvatar, category, submitted, modules, lessons,
                duration, previewRating, thumbnail, status, priority, description, mentorId, submittedAt,
                null, null, null);
    }

    private static CourseApproval approval(String courseId, String title, String mentor, String mentorAvatar,
                                           String category, String submitted, int modules, int lessons,
                                           String duration, double previewRating, String thumbnail,
                                           String status, String priority, String description,
                                           Long mentorId, Instant submittedAt, Instant reviewedAt,
                                           Long reviewedBy, String rejectionReason) {
        return CourseApproval.builder()
                .courseId(courseId)
                .title(title)
                .mentor(mentor)
                .mentorAvatar(mentorAvatar)
                .category(category)
                .submitted(submitted)
                .modules(modules)
                .lessons(lessons)
                .duration(duration)
                .previewRating(previewRating)
                .thumbnail(thumbnail)
                .status(status)
                .priority(priority)
                .description(description)
                .mentorId(mentorId)
                .submittedAt(submittedAt)
                .reviewedAt(reviewedAt)
                .reviewedBy(reviewedBy)
                .rejectionReason(rejectionReason)
                .build();
    }

    private static PlatformSetting setting(String key, String value, Instant updatedAt) {
        return PlatformSetting.builder()
                .key(key)
                .value(value)
                .updatedAt(updatedAt)
                .updatedBy(1L)
                .build();
    }

    private static FinancialTransaction transaction(String id, String type, BigDecimal amount, BigDecimal platformCut,
                                                    Long userId, String referenceId, String student, String course,
                                                    String dateLabel, Instant createdAt) {
        return FinancialTransaction.builder()
                .id(id)
                .type(type)
                .amount(amount)
                .platformCut(platformCut)
                .userId(userId)
                .referenceId(referenceId)
                .student(student)
                .course(course)
                .dateLabel(dateLabel)
                .createdAt(createdAt)
                .build();
    }
}
