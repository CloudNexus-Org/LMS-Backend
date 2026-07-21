package com.lms.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "track_id")
    private String trackId;

    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}
