package com.lms.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessPayoutRequest {

    Long mentorId;
    String mentorName;
    BigDecimal amount;
    String period;
}
