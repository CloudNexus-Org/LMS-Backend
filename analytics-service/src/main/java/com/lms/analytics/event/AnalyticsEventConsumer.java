package com.lms.analytics.event;

import com.lms.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "user.registered", groupId = "analytics-service")
    public void onUserRegistered(Map<String, Object> event) {
        log.info("Received user.registered: {}", event);
        analyticsService.recordUserRegistered();
    }

    @KafkaListener(topics = "payment.success", groupId = "analytics-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        log.info("Received payment.success: {}", event);
        Double amount = toDouble(event.get("amount"));
        analyticsService.recordPaymentSuccess(amount != null ? amount : 0);
    }

    @KafkaListener(topics = "lesson.completed", groupId = "analytics-service")
    public void onLessonCompleted(Map<String, Object> event) {
        log.info("Received lesson.completed: {}", event);
        Long userId = toLong(event.get("userId"));
        if (userId != null) {
            analyticsService.recordLessonCompleted(userId);
        }
    }

    @KafkaListener(topics = "track.completed", groupId = "analytics-service")
    public void onTrackCompleted(Map<String, Object> event) {
        log.info("Received track.completed: {}", event);
        analyticsService.recordTrackCompleted();
    }

    @KafkaListener(topics = "enrollment.created", groupId = "analytics-service")
    public void onEnrollmentCreated(Map<String, Object> event) {
        log.info("Received enrollment.created: {}", event);
        analyticsService.recordEnrollment();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return Long.valueOf(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        return Double.valueOf(value.toString());
    }
}
