package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentDashboardResponse {
    int totalEnrollments;
    int inProgress;
    int completed;
    int totalLessonsCompleted;
    double averageProgress;
}
