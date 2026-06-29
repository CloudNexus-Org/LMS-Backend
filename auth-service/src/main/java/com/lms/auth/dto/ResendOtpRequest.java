package com.lms.auth.dto;

import lombok.Data;

@Data
public class ResendOtpRequest {

    private String email;
    private String purpose = "PASSWORD_RESET";
}
