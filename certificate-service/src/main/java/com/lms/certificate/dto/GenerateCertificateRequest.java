package com.lms.certificate.dto;

import lombok.Data;

@Data
public class GenerateCertificateRequest {
    private Long userId;
    private String trackId;
    private String recipientName;
}
