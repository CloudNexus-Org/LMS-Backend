package com.lms.payment.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long courseId;
    private String trackId;
    /** "course" or "track" */
    private String itemType;
}
