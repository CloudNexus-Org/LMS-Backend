package com.lms.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InitiatePaymentResponse {
    /** Internal order id */
    private Long orderId;
    /** Razorpay order_xxx id — null for free courses */
    private String razorpayOrderId;
    /** Razorpay key_id (public, safe to send to frontend) */
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal total;
    /**
     * true when total == 0 (free course).
     * Frontend should skip Razorpay modal and directly enroll.
     */
    private boolean free;
}
