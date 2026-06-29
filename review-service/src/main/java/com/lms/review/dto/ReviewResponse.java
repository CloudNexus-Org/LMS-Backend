package com.lms.review.dto;

import com.lms.review.model.Review;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ReviewResponse {
    Long id;
    Long courseId;
    Long userId;
    String reviewerName;
    Integer rating;
    String title;
    String body;
    Integer helpful;
    Instant createdAt;
    Instant updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourseId())
                .userId(review.getUserId())
                .reviewerName(review.getReviewerName())
                .rating(review.getRating())
                .title(review.getTitle())
                .body(review.getBody())
                .helpful(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
