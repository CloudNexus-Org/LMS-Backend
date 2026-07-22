package com.lms.enrollment.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuizAttemptRequest {
    private String trackId;
    private Integer score;
    private Integer totalQuestions;
    private Integer passingScore;
    private Boolean passed;
    private List<Map<String, Object>> answers;
    /** When true, lesson cannot be marked complete until quiz is passed. */
    private Boolean requiresPass;
}
