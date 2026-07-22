package com.lms.enrollment.dto;

import lombok.Data;

@Data
public class CompleteLessonRequest {
    private String trackId;
    /** When true, refuse completion unless the lesson quiz has been passed. */
    private Boolean requireQuizPass;
}
