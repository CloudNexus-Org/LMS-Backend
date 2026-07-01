package com.lms.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendNotificationRequest {
    private List<Long> userIds;
    private String type;
    private String title;
    private String message;
    private String link;
    private String actionLabel;
    private String priority;
    private boolean broadcast;
}
