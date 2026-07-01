package com.lms.learning.dto;

import lombok.Data;

@Data
public class SessionRequest {
    private String trackId;
    private Long lastLessonId;
    private Integer lastPositionSec;
}
