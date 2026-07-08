package com.lms.admin.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCourseApproved(String courseId, Long numericCourseId, Long mentorId, String title) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("courseId", numericCourseId);
        payload.put("courseCode", courseId);
        payload.put("mentorId", mentorId);
        payload.put("title", title);
        kafkaTemplate.send("course.approved", courseId, payload);
    }

    public void publishCourseRejected(String courseId, Long mentorId, String title, String reason) {
        kafkaTemplate.send("course.rejected", courseId, Map.of(
                "courseCode", courseId,
                "mentorId", mentorId,
                "title", title,
                "reason", reason
        ));
    }
}
