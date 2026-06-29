package com.lms.admin.dto;

import com.lms.admin.model.FinancialTransaction;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransactionResponse {

    String id;
    String type;
    BigDecimal amount;
    BigDecimal cut;
    String date;
    String student;
    String course;

    public static TransactionResponse from(FinancialTransaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .cut(tx.getPlatformCut())
                .date(tx.getDateLabel())
                .student(tx.getStudent())
                .course(tx.getCourse())
                .build();
    }
}
