package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MyCourseResponse {
    Long id;
    String trackId;
    Long courseId;
    String title;
    String image;
    Integer progress;
    String status;
    Integer totalLessons;
    Integer completedLessons;
    String badge;
    String instructor;
    String rating;
    String duration;
    String modules;
    String description;
}
