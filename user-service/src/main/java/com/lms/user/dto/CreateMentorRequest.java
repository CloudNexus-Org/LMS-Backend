package com.lms.user.dto;

import lombok.Data;

@Data
public class CreateMentorRequest {
    String fullName;
    String username;
    String email;
    String password;
    String professionalRole;
    String company;
    String trackLabel;
    String location;
    String bio;
}
