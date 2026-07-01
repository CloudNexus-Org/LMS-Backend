package com.lms.enrollment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "track_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrackProgress {
    @EmbeddedId
    private TrackProgressId id;

    @Builder.Default
    private Integer completedLessons = 0;

    @Builder.Default
    private Integer totalLessons = 0;

    @Builder.Default
    private Integer progressPct = 0;

    private Long lastLessonId;

    @UpdateTimestamp
    private Instant updatedAt;
}
