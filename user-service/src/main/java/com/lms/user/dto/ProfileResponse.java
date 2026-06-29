package com.lms.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfileResponse {
    Long id;
    String email;
    String fullName;
    String role;
    String avatar;
    String phone;
    String bio;
    String status;
    String joined;
    String lastActive;
    SettingsResponse settings;
}
