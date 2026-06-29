package com.lms.admin.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialSummaryResponse {

    long totalSales;
    long mentorPayouts;
    long netRevenue;
    long platformCut;
    int recentSalesCount;
    String currency;
}
