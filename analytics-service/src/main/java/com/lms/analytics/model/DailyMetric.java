package com.lms.analytics.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_metrics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyMetric {
    @Id
    private LocalDate date;

    @Builder.Default
    private Integer totalUsers = 0;

    @Builder.Default
    private Integer newUsers = 0;

    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Builder.Default
    private Integer enrollments = 0;

    @Builder.Default
    private Integer completions = 0;
}
