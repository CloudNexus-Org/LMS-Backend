package com.lms.content.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_transcripts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonTranscript {

    @Id
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "transcript_text", columnDefinition = "TEXT", nullable = false)
    private String transcriptText;

    @Column(nullable = false)
    @Builder.Default
    private String language = "en";
}
