package com.lms.learning.dto;

import com.lms.learning.model.Note;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class NoteResponse {
    Long id;
    Long lessonId;
    String trackId;
    String content;
    Instant createdAt;
    Instant updatedAt;

    public static NoteResponse from(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .lessonId(note.getLessonId())
                .trackId(note.getTrackId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
