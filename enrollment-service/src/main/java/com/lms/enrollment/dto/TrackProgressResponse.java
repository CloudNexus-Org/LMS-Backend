package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TrackProgressResponse {
    String trackId;
    Integer progress;
    Integer completedLessons;
    Integer totalLessons;
    Long lastLessonId;
    List<Long> completedLessonIds;
    List<Long> quizPassedLessonIds;
}
