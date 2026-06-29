package com.lms.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentSummaryResponse {
    Long id;
    String fullName;
    String email;
    String role;
    String avatar;
    String status;
    String joined;
    String lastActive;
    String bio;
    String location;
}
