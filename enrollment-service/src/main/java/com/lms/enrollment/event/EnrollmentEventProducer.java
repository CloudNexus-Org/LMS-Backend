package com.lms.enrollment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EnrollmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLessonCompleted(Long userId, String trackId, Long lessonId) {
        kafkaTemplate.send("lesson.completed", trackId, Map.of(
                "userId", userId,
                "trackId", trackId,
                "lessonId", lessonId
        ));
    }

    public void publishTrackCompleted(Long userId, String trackId) {
        kafkaTemplate.send("track.completed", trackId, Map.of(
                "userId", userId,
                "trackId", trackId
        ));
    }

    public void publishProgressCompleted(Long userId, String trackId, Integer progressPct) {
        kafkaTemplate.send("progress.completed", trackId, Map.of(
                "userId", userId,
                "trackId", trackId,
                "progressPct", progressPct
        ));
    }

    public void publishEnrollmentCreated(Long userId, String trackId, Long enrollmentId) {
        kafkaTemplate.send("enrollment.created", trackId, Map.of(
                "userId", userId,
                "trackId", trackId,
                "enrollmentId", enrollmentId
        ));
    }
}
