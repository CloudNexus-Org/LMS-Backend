package com.lms.content.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class MentorClient {

    private final RestClient restClient;
    private final String userServiceUrl;

    public MentorClient(@Value("${lms.user-service-url:http://localhost:8082}") String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.restClient = RestClient.create();
    }

    public Optional<String> resolveMentorName(Long userId) {
        return fetchProfile(userId).map(this::displayName);
    }

    public boolean isActiveMentor(Long userId) {
        return fetchProfile(userId)
                .filter(profile -> "MENTOR".equalsIgnoreCase(String.valueOf(profile.get("role"))))
                .filter(profile -> !"Deleted".equalsIgnoreCase(String.valueOf(profile.get("status"))))
                .isPresent();
    }

    private Optional<Map<String, Object>> fetchProfile(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(userServiceUrl + "/api/users/profile")
                    .header("X-User-Id", String.valueOf(userId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (Exception ex) {
            log.warn("Mentor lookup failed for user {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    private String displayName(Map<String, Object> body) {
        if (body.get("fullName") != null && !body.get("fullName").toString().isBlank()) {
            return body.get("fullName").toString().trim();
        }
        if (body.get("name") != null && !body.get("name").toString().isBlank()) {
            return body.get("name").toString().trim();
        }
        if (body.get("email") != null) {
            return body.get("email").toString();
        }
        return "Mentor";
    }
}
