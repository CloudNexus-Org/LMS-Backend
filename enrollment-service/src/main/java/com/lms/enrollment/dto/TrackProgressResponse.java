package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TrackProgressResponse {
    String trackId;
    Integer progress;
    Integer completedLessons;
    Integer totalLessons;
    Long lastLessonId;
}
