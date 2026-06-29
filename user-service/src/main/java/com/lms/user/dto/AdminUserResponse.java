package com.lms.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserResponse {
    Long id;
    String name;
    String email;
    String role;
    String joined;
    String status;
    Integer courses;
    String lastActive;
    String avatar;
    String spend;
    String username;
    String professionalRole;
    String company;
    String trackLabel;
    String location;
    String bio;
}
