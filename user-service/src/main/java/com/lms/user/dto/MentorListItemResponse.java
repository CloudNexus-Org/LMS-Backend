package com.lms.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MentorListItemResponse {
    Long id;
    String name;
    String email;
    String role;
    String joined;
    String status;
    String lastActive;
    String avatar;
    String username;
    String professionalRole;
    String company;
    String trackLabel;
    String location;
    String bio;
}
