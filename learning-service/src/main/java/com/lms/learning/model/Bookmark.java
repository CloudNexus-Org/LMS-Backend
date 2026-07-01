package com.lms.learning.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "bookmarks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lessonId;

    private String trackId;

    private String title;

    @CreationTimestamp
    private Instant createdAt;
}
