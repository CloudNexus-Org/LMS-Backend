package com.lms.user.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    String fullName;
    String phone;
    String bio;
    String location;
    String username;
    String professionalRole;
}
