package com.lms.auth.dto;

import lombok.Data;

@Data
public class ProvisionCredentialRequest {
    private Long userId;
    private String email;
    private String password;
    private String fullName;
    private String role;
}
