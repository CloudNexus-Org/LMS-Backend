package com.lms.analytics.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class CourseMetricId implements Serializable {
    private Long courseId;
    private LocalDate date;
}
