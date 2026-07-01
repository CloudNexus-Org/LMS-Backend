package com.lms.enrollment.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnrollmentCheckResponse {
    boolean enrolled;
    String status;
    Long enrollmentId;
}
