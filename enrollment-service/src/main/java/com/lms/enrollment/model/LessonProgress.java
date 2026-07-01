package com.lms.enrollment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id", "track_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lessonId;

    @Column(nullable = false)
    private String trackId;

    @Builder.Default
    private Boolean completed = false;

    private Instant completedAt;

    @Builder.Default
    private Integer watchDurationSec = 0;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
