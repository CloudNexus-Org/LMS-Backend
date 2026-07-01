package com.lms.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String content;
    private String link;
    private String actionLabel;
    private String priority;
    private boolean read;
    private boolean unread;
    private Instant createdAt;
    private String time;
    private ActionDto action;

    @Data
    @Builder
    public static class ActionDto {
        private String label;
        private String to;
    }
}
