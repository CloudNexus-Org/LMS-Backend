package com.lms.learning.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "learning_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String trackId;

    private Long lastLessonId;

    @Builder.Default
    private Integer lastPositionSec = 0;

    @UpdateTimestamp
    private Instant updatedAt;
}
