package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private Long courseId;
    private Long mentorId;
    private String status;
    private String pricingPlan;
    private Double price;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String level;
    private String language;
    private List<String> outcomes;
    private List<String> tags;
    private String requirements;
    private String trackId;
    private String thumbnailUrl;
    private Instant submittedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private int moduleCount;
    private int lessonCount;
    private List<ModuleResponse> modules;
}
