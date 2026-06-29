package com.lms.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "mentor_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorPayout {

    @Id
    private String id;

    @Column(nullable = false)
    private Long mentorId;

    private String mentorName;

    @Column(nullable = false)
    private BigDecimal amount;

    private String period;

    @Column(nullable = false)
    private String status;

    private Instant processedAt;
}
