package com.lms.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCourseRequest {
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String level;
    private String language;
    private List<String> outcomes;
    private List<String> tags;
    private String requirements;
    private String trackId;
    private String thumbnailUrl;
}
