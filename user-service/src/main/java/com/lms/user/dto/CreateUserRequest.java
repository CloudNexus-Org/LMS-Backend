package com.lms.user.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    String email;
    String fullName;
    String role;
    String phone;
    String bio;
}
