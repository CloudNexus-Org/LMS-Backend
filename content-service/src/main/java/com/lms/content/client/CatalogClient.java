package com.lms.content.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
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

    public Optional<Long> syncPendingFromContent(SyncPayload payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.post()
                    .uri(catalogServiceUrl + "/api/catalog/internal/courses/sync-from-content")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toMap())
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("courseId") == null) {
                return Optional.empty();
            }
            return Optional.of(Long.valueOf(body.get("courseId").toString()));
        } catch (Exception ex) {
            log.warn("Catalog sync failed for content {}: {}", payload.contentId(), ex.getMessage());
            return Optional.empty();
        }
    }

    public record SyncPayload(
            Long contentId,
            Long catalogCourseId,
            Long mentorId,
            String title,
            String description,
            String category,
            String level,
            int modules,
            int lessons,
            BigDecimal price,
            String thumbnailUrl,
            String mentorName,
            List<String> outcomes,
            List<String> tags,
            String roadmap,
            String instructors
    ) {
        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("contentId", contentId);
            if (catalogCourseId != null) {
                map.put("catalogCourseId", catalogCourseId);
            }
            map.put("mentorId", mentorId);
            map.put("title", title != null ? title : "");
            map.put("description", description != null ? description : "");
            map.put("category", category != null ? category : "General");
            map.put("level", level != null ? level : "Beginner");
            map.put("modules", modules);
            map.put("lessons", lessons);
            map.put("price", price != null ? price : BigDecimal.ZERO);
            map.put("thumbnailUrl", thumbnailUrl != null ? thumbnailUrl : "");
            map.put("mentorName", mentorName != null ? mentorName : "Mentor");
            map.put("outcomes", outcomes != null ? outcomes : List.of());
            map.put("tags", tags != null ? tags : List.of());
            map.put("roadmap", roadmap != null ? roadmap : "");
            map.put("instructors", instructors != null ? instructors : "");
            return map;
        }
    }
}
