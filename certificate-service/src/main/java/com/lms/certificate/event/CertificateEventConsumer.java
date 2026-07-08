package com.lms.certificate.event;

import com.lms.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateEventConsumer {

    private final CertificateService certificateService;

    @KafkaListener(topics = "track.completed", groupId = "certificate-service")
    public void onTrackCompleted(Map<String, Object> event) {
        log.info("Received track.completed: {}", event);
        Long userId = toLong(event.get("userId"));
        String trackId = event.get("trackId") != null ? event.get("trackId").toString() : null;
        if (userId == null || trackId == null) {
            return;
        }
        try {
            certificateService.generateFromTrackCompletion(userId, trackId, null);
        } catch (Exception ex) {
            log.warn("Certificate auto-issue skipped for user {} track {}: {}", userId, trackId, ex.getMessage());
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return Long.valueOf(value.toString());
    }
}
