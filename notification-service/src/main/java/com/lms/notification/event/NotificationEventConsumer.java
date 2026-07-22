package com.lms.notification.event;

import com.lms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.registered", groupId = "notification-service")
    public void onUserRegistered(Map<String, Object> event) {
        Long userId = longVal(event.get("userId"));
        String fullName = stringVal(event.get("fullName"));
        if (userId == null) return;
        notificationService.createNotification(
                userId,
                "system",
                "Welcome to Realm!",
                "Hi " + (fullName != null ? fullName : "there") + "! Your account is ready. Explore courses and start learning.",
                "/student/catalog",
                "Browse courses",
                "normal",
                false
        );
        log.info("Welcome notification created for user {}", userId);
    }

    @KafkaListener(topics = "enrollment.created", groupId = "notification-service")
    public void onEnrollmentCreated(Map<String, Object> event) {
        Long userId = longVal(event.get("userId"));
        String trackId = stringVal(event.get("trackId"));
        if (userId == null) return;

        String courseTitle = stringVal(event.get("courseTitle"));
        String trackLabel = courseTitle != null && !courseTitle.isBlank()
                ? "\"" + courseTitle + "\""
                : (trackId != null && !trackId.isBlank() ? trackId + " track" : "your course");

        notificationService.createNotification(
                userId,
                "course",
                "Enrollment confirmed",
                "You're enrolled in " + trackLabel + ". Start learning right away.",
                trackId != null && !trackId.isBlank() ? "/learn/" + trackId : "/student/courses",
                "Start learning",
                "normal",
                false
        );

        Long mentorId = longVal(event.get("mentorId"));
        if (mentorId == null || mentorId.equals(userId)) {
            return;
        }

        String studentName = stringVal(event.get("studentName"));
        if (studentName == null || studentName.isBlank()) {
            studentName = "Student #" + userId;
        }
        String courseLabel = courseTitle != null && !courseTitle.isBlank()
                ? "\"" + courseTitle + "\""
                : (trackId != null && !trackId.isBlank() ? "\"" + trackId + "\"" : "your course");
        String whenLabel = formatPurchaseDateTime(stringVal(event.get("purchasedAt")));

        notificationService.createNotification(
                mentorId,
                "enrollment",
                "Course purchased",
                studentName + " has purchased " + courseLabel + " on " + whenLabel + ".",
                "/mentor/students",
                "View student",
                "normal",
                false
        );
        log.info("Purchase notification created for mentor {} (student={}, course={})",
                mentorId, userId, courseLabel);
    }

    @KafkaListener(topics = "payment.success", groupId = "notification-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        Long userId = longVal(event.get("userId"));
        Object amount = event.get("amount");
        if (userId == null) return;
        String amountLabel = amount != null ? String.valueOf(amount) : "your payment";
        notificationService.createNotification(
                userId,
                "system",
                "Payment successful",
                "Payment of " + amountLabel + " was processed successfully.",
                "/student/courses",
                "My courses",
                "normal",
                false
        );
    }

    @KafkaListener(topics = "certificate.issued", groupId = "notification-service")
    public void onCertificateIssued(Map<String, Object> event) {
        Long userId = longVal(event.get("userId"));
        String trackId = stringVal(event.get("trackId"));
        if (userId == null) return;
        notificationService.createNotification(
                userId,
                "achievement",
                "Certificate unlocked!",
                "Congratulations! You earned a certificate" + (trackId != null ? " for " + trackId + " track" : "") + ".",
                "/student/certificates",
                "View certificate",
                "normal",
                false
        );
    }

    @KafkaListener(topics = "course.approved", groupId = "notification-service")
    public void onCourseApproved(Map<String, Object> event) {
        Long mentorId = longVal(event.get("mentorId"));
        String title = stringVal(event.get("title"));
        if (mentorId == null) mentorId = 2L;
        notificationService.createNotification(
                mentorId,
                "approval",
                "Course approved & live",
                "\"" + (title != null ? title : "Your course") + "\" passed QA review and is now live on the marketplace.",
                "/mentor/lessons",
                "View course",
                "normal",
                false
        );
    }

    @KafkaListener(topics = "course.rejected", groupId = "notification-service")
    public void onCourseRejected(Map<String, Object> event) {
        Long mentorId = longVal(event.get("mentorId"));
        String title = stringVal(event.get("title"));
        String reason = stringVal(event.get("reason"));
        if (mentorId == null) mentorId = 2L;
        notificationService.createNotification(
                mentorId,
                "approval",
                "Course needs revision",
                "\"" + (title != null ? title : "Your course") + "\" was returned for changes"
                        + (reason != null ? ": " + reason : "."),
                "/mentor/upload",
                "Revise course",
                "high",
                false
        );
    }

    @KafkaListener(topics = "course.submitted", groupId = "notification-service")
    public void onCourseSubmitted(Map<String, Object> event) {
        String title = stringVal(event.get("title"));
        notificationService.createNotification(
                1L,
                "approval",
                "New course awaiting review",
                "\"" + (title != null ? title : "A mentor course") + "\" was submitted and needs QA review.",
                "/admin/approvals",
                "Review now",
                "high",
                false
        );
    }

    @KafkaListener(topics = "review.created", groupId = "notification-service")
    public void onReviewCreated(Map<String, Object> event) {
        Object rating = event.get("avgRating");
        notificationService.createNotification(
                2L,
                "review",
                "New course review received",
                "A student left a new review" + (rating != null ? " (avg " + rating + " stars)" : "") + ".",
                "/mentor/analytics",
                "View review",
                "normal",
                false
        );
    }

    @KafkaListener(topics = "otp.sent", groupId = "notification-service")
    public void onOtpSent(Map<String, Object> event) {
        String email = stringVal(event.get("email"));
        String code = stringVal(event.get("code"));
        String purpose = stringVal(event.get("purpose"));
        Long userId = longVal(event.get("userId"));
        log.info("OTP event received for {} (purpose={})", email, purpose);
        if (userId != null && code != null) {
            notificationService.createNotification(
                    userId,
                    "system",
                    "Password reset code",
                    "Your verification code is " + code + ". It expires in a few minutes.",
                    "/verify-otp",
                    "Enter code",
                    "high",
                    false
            );
        }
    }

    private static Long longVal(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString() : null;
    }

    private static final DateTimeFormatter PURCHASE_WHEN =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH);

    private static String formatPurchaseDateTime(String iso) {
        if (iso == null || iso.isBlank()) {
            return PURCHASE_WHEN.format(ZonedDateTime.now());
        }
        try {
            Instant instant = Instant.parse(iso);
            return PURCHASE_WHEN.format(instant.atZone(ZoneId.systemDefault()));
        } catch (Exception ex) {
            return iso;
        }
    }
}
