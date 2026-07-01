package com.lms.notification.dto;

import lombok.Data;

@Data
public class PreferencesRequest {
    private Boolean emailEnrollment;
    private Boolean emailPayment;
    private Boolean emailAssignment;
    private Boolean emailCertificate;
    private Boolean pushEnabled;
}
