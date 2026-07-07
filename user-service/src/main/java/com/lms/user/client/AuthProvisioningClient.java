package com.lms.user.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Component
public class AuthProvisioningClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.auth.url:http://localhost:8081}")
    private String authServiceUrl;

    public void provisionMentor(Long userId, String email, String password, String fullName) {
        try {
            restClient.post()
                    .uri(authServiceUrl + "/api/auth/internal/provision")
                    .body(Map.of(
                            "userId", userId,
                            "email", email,
                            "password", password,
                            "fullName", fullName,
                            "role", "MENTOR"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Provisioned auth credentials for mentor userId={} email={}", userId, email);
        } catch (Exception ex) {
            log.error("Failed to provision auth credentials for mentor {}: {}", email, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to create login credentials for mentor. Please try again.");
        }
    }
}
