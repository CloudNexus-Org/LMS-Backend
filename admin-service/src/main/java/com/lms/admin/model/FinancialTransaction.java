package com.lms.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "financial_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransaction {

    @Id
    private String id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal amount;

    private BigDecimal platformCut;

    private Long userId;

    private String referenceId;

    private String student;

    private String course;

    private String dateLabel;

    private Instant createdAt;
}
