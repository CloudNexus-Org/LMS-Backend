package com.lms.mentor.dto;

import lombok.Data;

import java.util.List;

@Data
public class MentorProfileUpdateRequest {
    private String bio;
    private String longBio;
    private String location;
    private Boolean available;
    private List<String> specialties;
}
