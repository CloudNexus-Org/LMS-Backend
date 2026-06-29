package com.lms.auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;
    private String code;
    private String newPassword;
    private String purpose = "PASSWORD_RESET";
}
