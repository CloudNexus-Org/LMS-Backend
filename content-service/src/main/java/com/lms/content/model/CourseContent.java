package com.lms.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "courses_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status", nullable = false)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "pricing_plan")
    private String pricingPlan;

    private Double price;

    private String title;
    private String subtitle;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    private String level;
    private String language;

    @Column(name = "outcomes_json", columnDefinition = "TEXT")
    private String outcomesJson;

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "track_id")
    private String trackId;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
