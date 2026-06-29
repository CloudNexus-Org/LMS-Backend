package com.lms.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SettingsResponse {
    String theme;
    String language;
    Boolean emailNotifications;
    Boolean pushNotifications;
}
