package com.lms.certificate.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class UserClient {

    private final RestClient restClient;
    private final String userServiceUrl;

    public UserClient(@Value("${lms.user-service-url:http://localhost:8082}") String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.restClient = RestClient.create();
    }

    public Optional<String> displayName(Long userId) {
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
            String fullName = stringVal(body.get("fullName"));
            if (!fullName.isBlank()) {
                return Optional.of(fullName);
            }
            String username = stringVal(body.get("username"));
            return username.isBlank() ? Optional.empty() : Optional.of(username);
        } catch (Exception ex) {
            log.warn("User lookup failed for id {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString().trim() : "";
    }
}
