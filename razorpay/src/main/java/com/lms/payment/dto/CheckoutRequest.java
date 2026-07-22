package com.lms.payment.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    /** List of courseIds to checkout; if empty, use full cart */
    private List<Long> courseIds;
    private String couponCode;
    private String trackId;
}
