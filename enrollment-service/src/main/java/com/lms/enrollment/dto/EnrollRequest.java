package com.lms.enrollment.dto;

import lombok.Data;

@Data
public class EnrollRequest {
    private String trackId;
    private Long courseId;
}
