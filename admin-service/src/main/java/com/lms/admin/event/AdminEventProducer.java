package com.lms.admin.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCourseApproved(String courseId, Long numericCourseId, Long mentorId, String title) {
        kafkaTemplate.send("course.approved", courseId, Map.of(
                "courseId", numericCourseId,
                "courseCode", courseId,
                "mentorId", mentorId,
                "title", title
        ));
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
