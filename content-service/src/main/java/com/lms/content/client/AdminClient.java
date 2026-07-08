package com.lms.content.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class AdminClient {

    private final RestClient restClient;
    private final String adminServiceUrl;

    public AdminClient(@Value("${lms.admin-service-url:http://localhost:8094}") String adminServiceUrl) {
        this.adminServiceUrl = adminServiceUrl;
        this.restClient = RestClient.create();
    }

    public void syncCourseSubmission(Map<String, Object> event) {
        try {
            restClient.post()
                    .uri(adminServiceUrl + "/api/admin/internal/approvals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Admin approval sync failed for {}: {}", event.get("courseCode"), ex.getMessage());
        }
    }
}
