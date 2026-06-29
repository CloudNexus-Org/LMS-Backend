package com.lms.user.dto;

import lombok.Data;

@Data
public class SettingsUpdateRequest {
    String theme;
    String language;
    Boolean emailNotifications;
    Boolean pushNotifications;
}
