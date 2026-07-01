package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CourseProgressResponse {
    Long courseId;
    String trackId;
    Integer progress;
    Integer completedLessons;
    Integer totalLessons;
    List<Long> completedLessonIds;
}
