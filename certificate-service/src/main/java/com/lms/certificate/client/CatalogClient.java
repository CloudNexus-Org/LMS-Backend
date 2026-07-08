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
public class CatalogClient {

    private final RestClient restClient;
    private final String catalogServiceUrl;

    public CatalogClient(@Value("${lms.catalog-service-url:http://localhost:8083}") String catalogServiceUrl) {
        this.catalogServiceUrl = catalogServiceUrl;
        this.restClient = RestClient.create();
    }

    public Optional<CourseSnapshot> findCourse(Long courseId) {
        if (courseId == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses/id/{courseId}", courseId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new CourseSnapshot(
                    courseId,
                    stringVal(body.get("title")),
                    stringVal(body.get("description")),
                    stringVal(body.get("duration")),
                    stringVal(body.get("difficulty")),
                    stringVal(body.get("professor")),
                    stringVal(body.get("exploreType"))
            ));
        } catch (Exception ex) {
            log.warn("Catalog lookup failed for course {}: {}", courseId, ex.getMessage());
            return Optional.empty();
        }
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString().trim() : "";
    }

    public record CourseSnapshot(
            Long courseId,
            String title,
            String description,
            String duration,
            String badge,
            String instructor,
            String exploreType
    ) {}
}
