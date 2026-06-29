package com.lms.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private String role;
    private String message;
}
