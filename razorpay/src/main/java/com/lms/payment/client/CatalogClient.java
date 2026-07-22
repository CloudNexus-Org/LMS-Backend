package com.lms.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
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

    public record CourseSnapshot(
            Long id,
            String title,
            BigDecimal price,
            BigDecimal originalPrice,
            String thumbnailUrl,
            String trackId
    ) {}

    @SuppressWarnings("unchecked")
    public Optional<CourseSnapshot> findCourse(Long courseId) {
        if (courseId == null) return Optional.empty();
        try {
            // Use the correct catalog endpoint: /api/catalog/courses/id/{courseId}
            Map<String, Object> body = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses/id/" + courseId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body == null) return Optional.empty();

            BigDecimal price = parseBigDecimal(body.get("price"));
            BigDecimal originalPrice = parseBigDecimal(body.get("originalPrice"));
            String title = body.getOrDefault("title", "Course").toString();
            String thumbnail = body.containsKey("thumbnailUrl") ? String.valueOf(body.get("thumbnailUrl")) : null;
            String trackIdRaw = body.containsKey("trackId") ? String.valueOf(body.get("trackId")) : null;

            return Optional.of(new CourseSnapshot(courseId, title, price, originalPrice, thumbnail, trackIdRaw));
        } catch (Exception ex) {
            log.warn("CatalogClient.findCourse failed for courseId={}: {}", courseId, ex.getMessage());
            return Optional.empty();
        }
    }

    private BigDecimal parseBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
