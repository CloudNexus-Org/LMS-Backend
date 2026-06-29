package com.lms.auth.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String email;
    private String code;
    private String purpose = "PASSWORD_RESET";
}
