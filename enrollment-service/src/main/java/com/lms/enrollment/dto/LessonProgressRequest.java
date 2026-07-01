package com.lms.enrollment.dto;

import lombok.Data;

@Data
public class LessonProgressRequest {
    private String trackId;
    private Integer watchDurationSec;
}
