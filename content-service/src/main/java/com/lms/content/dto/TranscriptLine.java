package com.lms.content.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranscriptLine {
    private String t;
    private int seconds;
    private String text;
}
