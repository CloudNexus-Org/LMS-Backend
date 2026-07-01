package com.lms.notification.service;

import com.lms.notification.dto.*;
import com.lms.notification.model.Notification;
import com.lms.notification.model.NotificationPreference;
import com.lms.notification.repository.NotificationPreferenceRepository;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public Page<NotificationResponse> myNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Map<String, Long> unreadCount(Long userId) {
        return Map.of("count", notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public Map<String, Object> markAllRead(Long userId) {
        int updated = notificationRepository.markAllRead(userId);
        return Map.of("updated", updated, "message", "All notifications marked as read");
    }

    @Transactional
    public Map<String, String> deleteNotification(Long userId, Long notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);
        notificationRepository.delete(notification);
        return Map.of("message", "Notification deleted");
    }

    public PreferencesResponse getPreferences(Long userId) {
        return toPreferencesResponse(getOrCreatePreferences(userId));
    }

    @Transactional
    public PreferencesResponse updatePreferences(Long userId, PreferencesRequest request) {
        NotificationPreference prefs = getOrCreatePreferences(userId);
        if (request.getEmailEnrollment() != null) prefs.setEmailEnrollment(request.getEmailEnrollment());
        if (request.getEmailPayment() != null) prefs.setEmailPayment(request.getEmailPayment());
        if (request.getEmailAssignment() != null) prefs.setEmailAssignment(request.getEmailAssignment());
        if (request.getEmailCertificate() != null) prefs.setEmailCertificate(request.getEmailCertificate());
        if (request.getPushEnabled() != null) prefs.setPushEnabled(request.getPushEnabled());
        return toPreferencesResponse(preferenceRepository.save(prefs));
    }

    @Transactional
    public List<NotificationResponse> sendNotification(String role, SendNotificationRequest request) {
        boolean isAdmin = role != null && "ADMIN".equalsIgnoreCase(role);
        boolean isInternal = role != null && "INTERNAL".equalsIgnoreCase(role);
        if (!isAdmin && !isInternal) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()
                || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and message are required");
        }

        List<Long> targets = new ArrayList<>();
        if (request.isBroadcast()) {
            targets.add(1L);
            targets.add(2L);
            targets.add(3L);
        } else if (request.getUserIds() != null) {
            targets.addAll(request.getUserIds());
        }
        if (targets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userIds or broadcast flag required");
        }

        String type = request.getType() != null ? request.getType() : "system";
        List<NotificationResponse> created = new ArrayList<>();
        for (Long userId : targets) {
            created.add(toResponse(createNotification(
                    userId,
                    type,
                    request.getTitle(),
                    request.getMessage(),
                    request.getLink(),
                    request.getActionLabel(),
                    request.getPriority(),
                    false
            )));
        }
        return created;
    }

    @Transactional
    public Notification createNotification(
            Long userId,
            String type,
            String title,
            String message,
            String link,
            String actionLabel,
            String priority,
            boolean read) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .actionLabel(actionLabel)
                .priority(priority)
                .read(read)
                .createdAt(Instant.now())
                .build();
        return notificationRepository.save(notification);
    }

    private Notification getOwnedNotification(Long userId, Long notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }

    private NotificationPreference getOrCreatePreferences(Long userId) {
        return preferenceRepository.findById(userId)
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.builder().userId(userId).build()));
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse.ActionDto action = null;
        if (n.getLink() != null && !n.getLink().isBlank()) {
            action = NotificationResponse.ActionDto.builder()
                    .label(n.getActionLabel() != null ? n.getActionLabel() : "View")
                    .to(n.getLink())
                    .build();
        }
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .content(n.getMessage())
                .link(n.getLink())
                .actionLabel(n.getActionLabel())
                .priority(n.getPriority())
                .read(n.isRead())
                .unread(!n.isRead())
                .createdAt(n.getCreatedAt())
                .time(formatRelativeTime(n.getCreatedAt()))
                .action(action)
                .build();
    }

    private PreferencesResponse toPreferencesResponse(NotificationPreference prefs) {
        return PreferencesResponse.builder()
                .userId(prefs.getUserId())
                .emailEnrollment(prefs.isEmailEnrollment())
                .emailPayment(prefs.isEmailPayment())
                .emailAssignment(prefs.isEmailAssignment())
                .emailCertificate(prefs.isEmailCertificate())
                .pushEnabled(prefs.isPushEnabled())
                .build();
    }

    static String formatRelativeTime(Instant createdAt) {
        if (createdAt == null) return "Recently";
        Duration diff = Duration.between(createdAt, Instant.now());
        long minutes = diff.toMinutes();
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " min ago" : " min ago");
        long hours = diff.toHours();
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = diff.toDays();
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 14) return "1 week ago";
        return (days / 7) + " weeks ago";
    }
}
