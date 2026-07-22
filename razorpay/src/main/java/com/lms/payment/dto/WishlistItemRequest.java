package com.lms.payment.dto;

import lombok.Data;

@Data
public class WishlistItemRequest {
    private Long courseId;
    private String trackId;
}
