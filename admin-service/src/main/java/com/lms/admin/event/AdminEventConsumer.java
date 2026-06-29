package com.lms.admin.event;

import com.lms.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventConsumer {

    private final AdminService adminService;

    @KafkaListener(topics = "course.submitted", groupId = "admin-service")
    public void onCourseSubmitted(Map<String, Object> event) {
        log.info("Received course.submitted: {}", event);
        adminService.createPendingApproval(event);
    }

    @KafkaListener(topics = "payment.success", groupId = "admin-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        log.info("Received payment.success (stub): {}", event);
    }
}
