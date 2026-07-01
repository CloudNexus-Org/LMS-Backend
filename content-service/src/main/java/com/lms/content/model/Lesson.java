package com.lms.content.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private String type = "video";

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @Column(name = "content_url")
    private String contentUrl;

    @Column(name = "reading_content", columnDefinition = "TEXT")
    private String readingContent;

    @Column(name = "is_preview_free", nullable = false)
    @Builder.Default
    private boolean previewFree = false;

    @Column(columnDefinition = "TEXT")
    private String summary;
}
