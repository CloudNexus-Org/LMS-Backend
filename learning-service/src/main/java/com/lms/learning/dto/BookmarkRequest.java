package com.lms.learning.dto;

import lombok.Data;

@Data
public class BookmarkRequest {
    private Long lessonId;
    private String trackId;
    private String title;
}
