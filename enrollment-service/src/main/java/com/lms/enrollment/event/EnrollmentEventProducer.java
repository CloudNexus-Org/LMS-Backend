package com.lms.enrollment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
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

    public void publishEnrollmentCreated(
            Long userId,
            String trackId,
            Long enrollmentId,
            Long courseId,
            Long mentorId,
            String courseTitle,
            String studentName,
            Instant purchasedAt
    ) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("userId", userId);
        event.put("trackId", trackId != null ? trackId : "");
        event.put("enrollmentId", enrollmentId);
        event.put("courseId", courseId != null ? courseId : 0);
        if (mentorId != null) {
            event.put("mentorId", mentorId);
        }
        if (courseTitle != null && !courseTitle.isBlank()) {
            event.put("courseTitle", courseTitle);
        }
        if (studentName != null && !studentName.isBlank()) {
            event.put("studentName", studentName);
        }
        event.put("purchasedAt", purchasedAt != null ? purchasedAt.toString() : Instant.now().toString());
        kafkaTemplate.send("enrollment.created", trackId != null ? trackId : String.valueOf(userId), event);
    }
}
