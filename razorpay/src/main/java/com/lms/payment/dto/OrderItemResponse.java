package com.lms.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long courseId;
    private String trackId;
    private String title;
    private BigDecimal price;
}
