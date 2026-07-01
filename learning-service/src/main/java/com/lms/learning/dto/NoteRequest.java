package com.lms.learning.dto;

import lombok.Data;

@Data
public class NoteRequest {
    private Long lessonId;
    private String trackId;
    private String content;
}
