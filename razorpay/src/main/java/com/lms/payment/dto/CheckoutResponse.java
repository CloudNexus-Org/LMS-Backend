package com.lms.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CheckoutResponse {
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal total;
    private BigDecimal savings;
    private String couponCode;
    private BigDecimal discount;
}
