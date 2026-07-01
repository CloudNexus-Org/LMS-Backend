package com.lms.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferencesResponse {
    private Long userId;
    private boolean emailEnrollment;
    private boolean emailPayment;
    private boolean emailAssignment;
    private boolean emailCertificate;
    private boolean pushEnabled;
}
