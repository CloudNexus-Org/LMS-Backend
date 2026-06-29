package com.lms.catalog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String role;

    private String company;

    private String course;

    private Integer rating;

    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String quote;
}
