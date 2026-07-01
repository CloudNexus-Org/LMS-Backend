package com.lms.notification.controller;

import com.lms.notification.dto.*;
import com.lms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public Page<NotificationResponse> myNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return notificationService.myNotifications(userId, page, size);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return notificationService.markRead(userId, id);
    }

    @PatchMapping("/read-all")
    public Map<String, Object> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.markAllRead(userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNotification(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return notificationService.deleteNotification(userId, id);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.unreadCount(userId);
    }

    @GetMapping("/preferences")
    public PreferencesResponse getPreferences(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.getPreferences(userId);
    }

    @PutMapping("/preferences")
    public PreferencesResponse updatePreferences(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PreferencesRequest request) {
        return notificationService.updatePreferences(userId, request);
    }

    @PostMapping("/send")
    public List<NotificationResponse> send(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody SendNotificationRequest request) {
        return notificationService.sendNotification(role, request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "notification-service");
    }
}
