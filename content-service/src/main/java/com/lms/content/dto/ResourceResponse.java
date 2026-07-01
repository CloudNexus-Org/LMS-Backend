package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceResponse {
    private Long id;
    private Long lessonId;
    private String title;
    private String fileUrl;
    private String fileType;
    private String label;
    private String meta;
    private String type;
}
