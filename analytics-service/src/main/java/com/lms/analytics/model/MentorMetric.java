package com.lms.analytics.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mentor_metrics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorMetric {
    @EmbeddedId
    private MentorMetricId id;

    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Builder.Default
    private Integer newStudents = 0;

    @Builder.Default
    private Integer activeStudents = 0;
}
