package com.lms.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long mentorId;

    private String professor;

    private String difficulty;

    private String duration;

    private Integer modules;

    private Integer lessons;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Double rating;

    private Integer reviewCount;

    private String enrolled;

    private String status;

    private String thumbnailUrl;

    private String exploreType;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    private Boolean freePreview = true;

    @ElementCollection
    @CollectionTable(name = "course_outcomes", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "outcome_text")
    @OrderColumn(name = "order_index")
    @Builder.Default
    private List<String> outcomes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "course_skills", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "skill_name")
    @Builder.Default
    private List<String> skills = new ArrayList<>();
}
