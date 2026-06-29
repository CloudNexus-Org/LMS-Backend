package com.lms.mentor.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String role;
    private String company;
    private String trackLabel;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String longBio;

    private String avatarUrl;
    private Double rating;
    private Integer reviewsCount;
    private String learnersCount;
    private Integer sessionsCount;
    private Integer yearsExp;
    private String location;

    @Builder.Default
    private Boolean available = true;

    @ElementCollection
    @CollectionTable(name = "mentor_specialties", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "specialty")
    @Builder.Default
    private List<String> specialties = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "mentor_achievements", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "achievement_text")
    @OrderColumn(name = "order_index")
    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MentorExperience> experience = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MentorTaughtCourse> taughtCourses = new ArrayList<>();
}
