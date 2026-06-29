package com.lms.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer durationWeeks;

    private String hoursPerWeek;

    private String level;

    private String leadMentorSlug;

    private String color;

    private String iconKey;

    private String salary;

    private String medianSalary;

    private Integer activeLearners;

    private String enrolled;

    private Double rating;

    private Integer reviews;

    private String badge;

    private String nextCohort;

    private String language;

    private String certificate;

    private String status;

    @ElementCollection
    @CollectionTable(name = "track_skills", joinColumns = @JoinColumn(name = "track_id"))
    @Column(name = "skill_name")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "track_outcomes", joinColumns = @JoinColumn(name = "track_id"))
    @Column(name = "outcome_text")
    @OrderColumn(name = "order_index")
    @Builder.Default
    private List<String> outcomes = new ArrayList<>();

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<TrackCourse> trackCourses = new ArrayList<>();
}
