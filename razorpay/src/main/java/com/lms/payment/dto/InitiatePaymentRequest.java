package com.lms.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InitiatePaymentRequest {
    /** courseIds to pay for; if empty, checkout full cart */
    private List<Long> courseIds;
    private String couponCode;
    private String trackId;
    private String currency;
    /**
     * Direct amount override (paise/rupees) — used when frontend
     * has track price from local data and courseId is not available.
     * If provided and non-zero, skips catalog lookup for pricing.
     */
    private BigDecimal amount;
    /** Human-readable title for the order item when no courseId exists */
    private String title;
}
