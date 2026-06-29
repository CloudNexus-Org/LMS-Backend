package com.lms.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    String email;
    String fullName;
    String role;
    String phone;
    String bio;
    String status;
    String professionalRole;
    String company;
    String trackLabel;
    String location;
}
