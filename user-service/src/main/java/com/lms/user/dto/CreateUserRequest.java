package com.lms.user.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    String email;
    String fullName;
    String role;
    String password;
    String phone;
    String bio;
}
