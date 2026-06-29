package com.lms.admin.dto;

import lombok.Data;

@Data
public class GenerateReportRequest {

    String period;
    String type;
    String format;
}
