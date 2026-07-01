package com.lms.content.dto;

import lombok.Data;

@Data
public class LessonRequest {
    private String title;
    private String type;
    private Integer durationMin;
    private Integer orderIndex;
    private String contentUrl;
    private String readingContent;
    private Boolean previewFree;
    private String summary;
}
