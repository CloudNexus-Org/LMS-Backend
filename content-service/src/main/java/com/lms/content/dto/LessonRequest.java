package com.lms.content.dto;

import lombok.Data;

import java.util.Map;

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
    /** Quiz payload object — serialized to quiz_json on the lesson. */
    private Map<String, Object> quiz;
    private Boolean uploadInProgress;
}
