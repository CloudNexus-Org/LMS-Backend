package com.lms.user.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserUpdated(Long userId, String email, String fullName, String role) {
        kafkaTemplate.send("user.updated", String.valueOf(userId), Map.of(
                "userId", userId,
                "email", email,
                "fullName", fullName,
                "role", role
        ));
    }

    public void publishUserBanned(Long userId, String email, String reason) {
        kafkaTemplate.send("user.banned", String.valueOf(userId), Map.of(
                "userId", userId,
                "email", email,
                "reason", reason == null ? "Admin action" : reason
        ));
    }

    public void publishMentorCreated(Map<String, Object> payload) {
        Object userId = payload.get("userId");
        kafkaTemplate.send("mentor.created", userId == null ? "mentor" : String.valueOf(userId), payload);
    }
}
