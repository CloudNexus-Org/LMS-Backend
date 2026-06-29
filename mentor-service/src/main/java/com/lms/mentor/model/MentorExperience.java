package com.lms.mentor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_experience")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    private String title;
    private String org;
    private String period;

    @Column(columnDefinition = "TEXT")
    private String description;
}
