package com.lms.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "course_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDraft {

    @Id
    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "draft_json", columnDefinition = "TEXT")
    private String draftJson;

    @Column(name = "last_saved_at")
    private Instant lastSavedAt;
}
