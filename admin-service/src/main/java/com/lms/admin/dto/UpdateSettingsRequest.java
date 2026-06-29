package com.lms.admin.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateSettingsRequest {

    private String platformName;
    private Double commissionPct;
    private Double gstRate;
    private String currency;
    private String supportEmail;
    private Map<String, String> settings;
}
