package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TranscriptResponse {
    private Long lessonId;
    private String language;
    private String transcriptText;
    private List<TranscriptLine> lines;
}
