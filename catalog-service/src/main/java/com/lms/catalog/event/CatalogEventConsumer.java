package com.lms.catalog.event;

import com.lms.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogEventConsumer {

    private final CatalogService catalogService;

    @KafkaListener(topics = "review.created", groupId = "catalog-service")
    public void onReviewCreated(Map<String, Object> event) {
        log.info("Received review.created: {}", event);
        Long courseId = toLong(event.get("courseId"));
        Double avgRating = toDouble(event.get("avgRating"));
        Integer reviewCount = toInt(event.get("reviewCount"));
        if (courseId != null && avgRating != null && reviewCount != null) {
            catalogService.updateCourseRating(courseId, avgRating, reviewCount);
        }
    }

    @KafkaListener(topics = "course.approved", groupId = "catalog-service")
    public void onCourseApproved(Map<String, Object> event) {
        log.info("Received course.approved: {}", event);
        Long courseId = toLong(event.get("courseId"));
        if (courseId != null) {
            catalogService.publishCourse(courseId);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return Long.valueOf(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        return Double.valueOf(value.toString());
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        return Integer.valueOf(value.toString());
    }
}
