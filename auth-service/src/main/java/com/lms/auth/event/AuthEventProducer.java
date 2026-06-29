package com.lms.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(Long userId, String email, String role, String fullName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("role", role);
        payload.put("fullName", fullName);
        send("user.registered", email, payload);
    }

    public void publishOtpSent(String email, String purpose, Instant expiresAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("purpose", purpose);
        payload.put("expiresAt", expiresAt.toString());
        send("otp.sent", email, payload);
    }

    public void publishPasswordReset(Long userId, String email) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("email", email);
        send("user.password-reset", email, payload);
    }

    private void send(String topic, String key, Map<String, Object> payload) {
        log.info("Publishing {} event: {}", topic, payload);
        kafkaTemplate.send(topic, key, payload);
    }
}
