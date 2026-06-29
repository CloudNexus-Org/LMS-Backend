package com.lms.admin.dto;

import com.lms.admin.model.MentorPayout;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class MentorPayoutResponse {

    String id;
    Long mentorId;
    String mentorName;
    BigDecimal amount;
    String period;
    String status;
    Instant processedAt;

    public static MentorPayoutResponse from(MentorPayout payout) {
        return MentorPayoutResponse.builder()
                .id(payout.getId())
                .mentorId(payout.getMentorId())
                .mentorName(payout.getMentorName())
                .amount(payout.getAmount())
                .period(payout.getPeriod())
                .status(payout.getStatus())
                .processedAt(payout.getProcessedAt())
                .build();
    }
}
