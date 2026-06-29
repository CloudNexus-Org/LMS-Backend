package com.lms.admin.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class SettingsResponse {

    Map<String, String> settings;
}
