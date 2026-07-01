package com.lms.certificate.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerifyResponse {
    boolean valid;
    String code;
    String recipient;
    String title;
    String track;
    String issueDate;
    String status;
}
