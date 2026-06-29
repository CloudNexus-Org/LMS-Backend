package com.lms.mentor.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MentorEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProfileUpdated(String slug, Long mentorId) {
        kafkaTemplate.send("mentor.profile-updated", slug, Map.of(
                "mentorId", mentorId,
                "slug", slug
        ));
    }
}
