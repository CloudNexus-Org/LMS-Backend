package com.lms.review.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer rating;
    private String title;
    private String body;
    private String reviewerName;
}
