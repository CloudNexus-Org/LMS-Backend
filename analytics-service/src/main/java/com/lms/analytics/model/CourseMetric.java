package com.lms.analytics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_metrics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseMetric {
    @EmbeddedId
    private CourseMetricId id;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    private Integer enrollments = 0;

    @Builder.Default
    private Integer completions = 0;

    @Builder.Default
    private Double avgRating = 0.0;
}
