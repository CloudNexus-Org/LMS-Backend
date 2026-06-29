package com.lms.review.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReviewCreated(Long courseId, Double avgRating, Integer reviewCount, Long reviewId) {
        kafkaTemplate.send("review.created", String.valueOf(courseId), Map.of(
                "reviewId", reviewId,
                "courseId", courseId,
                "avgRating", avgRating,
                "reviewCount", reviewCount
        ));
    }

    public void publishReviewUpdated(Long courseId, Double avgRating, Integer reviewCount, Long reviewId) {
        kafkaTemplate.send("review.updated", String.valueOf(courseId), Map.of(
                "reviewId", reviewId,
                "courseId", courseId,
                "avgRating", avgRating,
                "reviewCount", reviewCount
        ));
    }
}
