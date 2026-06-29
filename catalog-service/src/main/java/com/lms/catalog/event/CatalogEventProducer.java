package com.lms.catalog.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatalogEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCourseSubmitted(Map<String, Object> payload) {
        String courseCode = String.valueOf(payload.get("courseCode"));
        kafkaTemplate.send("course.submitted", courseCode, payload);
    }
}
