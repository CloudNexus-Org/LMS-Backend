package com.lms.analytics.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class MentorMetricId implements Serializable {
    private Long mentorId;
    private LocalDate date;
}
