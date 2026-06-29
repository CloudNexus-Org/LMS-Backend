package com.lms.mentor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_taught_courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorTaughtCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    private Long courseId;
    private String title;
    private String level;
    private Integer modules;
    private Integer hours;
}
