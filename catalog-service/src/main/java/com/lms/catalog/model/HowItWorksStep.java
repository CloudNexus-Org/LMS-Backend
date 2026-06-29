package com.lms.catalog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "how_it_works")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HowItWorksStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String step;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String icon;

    private Integer orderIndex;
}
