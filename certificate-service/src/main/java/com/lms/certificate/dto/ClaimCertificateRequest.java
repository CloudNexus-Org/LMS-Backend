package com.lms.certificate.dto;

import lombok.Data;

@Data
public class ClaimCertificateRequest {
    private String trackId;
    private Long courseId;
}
