package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class EnrollmentDetailResponse {
    Long id;
    Long userId;
    String trackId;
    Long courseId;
    String status;
    Integer progress;
    Integer completedLessons;
    Integer totalLessons;
    Long lastLessonId;
    Instant enrolledAt;
}
