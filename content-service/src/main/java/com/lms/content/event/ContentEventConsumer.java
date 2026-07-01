package com.lms.content.event;

import com.lms.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ContentEventConsumer {

    private final ContentService contentService;

    @KafkaListener(topics = "course.approved", groupId = "content-service")
    public void onCourseApproved(Map<String, Object> event) {
        contentService.handleCourseApproved(event);
        log.info("Handled course.approved: {}", event);
    }

    @KafkaListener(topics = "course.rejected", groupId = "content-service")
    public void onCourseRejected(Map<String, Object> event) {
        contentService.handleCourseRejected(event);
        log.info("Handled course.rejected: {}", event);
    }
}
