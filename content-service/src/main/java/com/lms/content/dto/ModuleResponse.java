package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModuleResponse {
    private Long id;
    private Long courseId;
    private String title;
    private int orderIndex;
    private String description;
    private List<LessonResponse> lessons;
}
