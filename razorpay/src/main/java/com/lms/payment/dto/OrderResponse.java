package com.lms.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal total;
    private String status;
    private String razorpayOrderId;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
