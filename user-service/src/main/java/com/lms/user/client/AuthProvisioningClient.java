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
        provisionCredential(userId, email, password, fullName, "MENTOR");
    }

    public void provisionCredential(
            Long userId, String email, String password, String fullName, String role) {
        String normalizedRole = role != null ? role.trim().toUpperCase() : "STUDENT";
        try {
            restClient.post()
                    .uri(authServiceUrl + "/api/auth/internal/provision")
                    .body(Map.of(
                            "userId", userId,
                            "email", email,
                            "password", password,
                            "fullName", fullName,
                            "role", normalizedRole))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Provisioned auth credentials for userId={} email={} role={}",
                    userId, email, normalizedRole);
        } catch (Exception ex) {
            log.error("Failed to provision auth credentials for {}: {}", email, ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to create login credentials. Please try again.");
        }
    }

    public void syncProfileName(String email, String fullName) {
        if (email == null || email.isBlank() || fullName == null || fullName.isBlank()) {
            return;
        }
        try {
            restClient.put()
                    .uri(authServiceUrl + "/api/auth/internal/profile-name")
                    .body(Map.of("email", email, "fullName", fullName))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Synced auth fullName for email={}", email);
        } catch (Exception ex) {
            log.warn("Failed to sync auth fullName for {}: {}", email, ex.getMessage());
        }
    }
}
