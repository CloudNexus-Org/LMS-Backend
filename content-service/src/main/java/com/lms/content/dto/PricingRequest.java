package com.lms.content.dto;

import lombok.Data;

@Data
public class PricingRequest {
    private String pricingPlan;
    private Double price;
}
