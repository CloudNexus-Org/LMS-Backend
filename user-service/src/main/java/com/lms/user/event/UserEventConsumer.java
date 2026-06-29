package com.lms.user.event;

import com.lms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserService userService;

    @KafkaListener(topics = "user.registered", groupId = "user-service")
    public void onUserRegistered(Map<String, Object> event) {
        log.info("Received user.registered: {}", event);
        Long userId = toLong(event.get("userId"));
        String email = stringValue(event.get("email"));
        String role = stringValue(event.getOrDefault("role", "STUDENT"));
        String fullName = stringValue(event.get("fullName"));
        if (userId != null && email != null) {
            userService.createProfileFromRegistration(userId, email, role, fullName);
        }
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
