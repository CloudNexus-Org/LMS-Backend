package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonResponse {
    private Long id;
    private Long moduleId;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String type;
    private Integer durationMin;
    private String duration;
    private int orderIndex;
    private String contentUrl;
    private String readingContent;
    private boolean previewFree;
    private boolean free;
    private String summary;
}
