package com.lms.enrollment.event;

import com.lms.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentEventConsumer {

    private final EnrollmentService enrollmentService;

    @KafkaListener(topics = "payment.success", groupId = "enrollment-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        log.info("Received payment.success: {}", event);
        Long userId = toLong(event.get("userId"));
        String trackId = event.get("trackId") != null ? event.get("trackId").toString() : null;
        Long courseId = toLong(event.get("courseId"));
        if (userId != null && trackId != null) {
            enrollmentService.enrollFromPayment(userId, trackId, courseId);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return Long.valueOf(value.toString());
    }
}
