package com.lms.admin.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AdminEventConsumer {

    @KafkaListener(topics = "payment.success", groupId = "admin-service")
    public void onPaymentSuccess(Map<String, Object> event) {
        log.info("Received payment.success (stub): {}", event);
    }
}
