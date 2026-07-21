package com.lms.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** "razorpay" */
    @Column(nullable = false)
    private String gateway;

    /** Razorpay pay_xxx */
    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    /** Razorpay order_xxx */
    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    /** Razorpay signature */
    private String signature;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** SUCCESS | FAILED | REFUNDED */
    @Column(nullable = false)
    private String status;

    @Column(name = "paid_at")
    private Instant paidAt;
}
