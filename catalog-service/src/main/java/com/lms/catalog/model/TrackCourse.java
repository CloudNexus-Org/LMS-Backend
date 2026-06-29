package com.lms.catalog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "track_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false)
    private Long courseId;

    private Integer orderIndex;
}
