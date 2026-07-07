package com.lms.auth.event;

import com.lms.auth.model.UserRole;
import com.lms.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final AuthService authService;

    @KafkaListener(topics = "mentor.created", groupId = "auth-service")
    public void onMentorCreated(Map<String, Object> event) {
        log.info("Received mentor.created for auth provisioning: userId={}", event.get("userId"));
        Long userId = toLong(event.get("userId"));
        String email = stringValue(event.get("email"));
        String password = stringValue(event.get("password"));
        String fullName = stringValue(event.get("name"));
        if (userId == null || email == null || password == null || fullName == null) {
            log.warn("mentor.created missing required auth fields, skipping: {}", event);
            return;
        }
        authService.provisionCredential(userId, email, password, fullName, UserRole.MENTOR);
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
