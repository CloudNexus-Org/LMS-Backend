package com.lms.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class WishlistItemResponse {
    private Long id;
    private Long courseId;
    private String trackId;
    private String title;
    private BigDecimal price;
    private String thumbnailUrl;
    private Instant addedAt;
}
