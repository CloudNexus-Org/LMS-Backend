package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class QuizAttemptResponse {
    Long id;
    Long lessonId;
    String trackId;
    Integer score;
    Integer totalQuestions;
    Integer passingScore;
    Boolean passed;
    Instant attemptedAt;
}
