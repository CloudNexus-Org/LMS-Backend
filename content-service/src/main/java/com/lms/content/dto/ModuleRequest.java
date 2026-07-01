package com.lms.content.dto;

import lombok.Data;

@Data
public class ModuleRequest {
    private String title;
    private String description;
    private Integer orderIndex;
}
