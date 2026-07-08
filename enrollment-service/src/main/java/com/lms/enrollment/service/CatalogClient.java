package com.lms.enrollment.service;

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
            return Optional.of(mapCourse(body, courseId));
        } catch (Exception ex) {
            log.warn("Catalog lookup failed for course {}: {}", courseId, ex.getMessage());
            return Optional.empty();
        }
    }

    public String resolveTrackId(Long courseId, String preferredTrackId) {
        if (preferredTrackId != null && !preferredTrackId.isBlank()) {
            return preferredTrackId;
        }
        if (courseId == null) {
            return "general";
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses/id/{courseId}/track", courseId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body != null && body.get("trackId") != null) {
                return body.get("trackId").toString();
            }
        } catch (Exception ex) {
            log.warn("Track resolve failed for course {}: {}", courseId, ex.getMessage());
        }
        return "course-" + courseId;
    }

    private CourseSnapshot mapCourse(Map<String, Object> body, Long courseId) {
        int lessons = toInt(body.get("lessons"), 0);
        return new CourseSnapshot(
                courseId,
                stringVal(body.get("title")),
                stringVal(body.get("image")),
                stringVal(body.get("difficulty")),
                stringVal(body.get("professor")),
                stringVal(body.get("rating")),
                stringVal(body.get("duration")),
                modulesLabel(body.get("modules")),
                stringVal(body.get("description")),
                lessons
        );
    }

    private static String modulesLabel(Object modules) {
        if (modules == null) {
            return "";
        }
        return modules + " Mod";
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString() : "";
    }

    private static int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public record CourseSnapshot(
            Long courseId,
            String title,
            String image,
            String badge,
            String instructor,
            String rating,
            String duration,
            String modules,
            String description,
            int totalLessons
    ) {}
}
