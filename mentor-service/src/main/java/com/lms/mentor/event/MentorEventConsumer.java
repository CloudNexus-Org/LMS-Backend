package com.lms.mentor.event;

import com.lms.mentor.model.Mentor;
import com.lms.mentor.model.MentorExperience;
import com.lms.mentor.model.MentorTaughtCourse;
import com.lms.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MentorEventConsumer {

    private final MentorRepository mentorRepository;

    @KafkaListener(topics = "mentor.created", groupId = "mentor-service")
    @Transactional
    public void onMentorCreated(Map<String, Object> event) {
        log.info("Received mentor.created: {}", event);
        if (mentorRepository.findByUserId(toLong(event.get("userId"))).isPresent()) {
            return;
        }
        Mentor mentor = Mentor.builder()
                .userId(toLong(event.get("userId")))
                .slug(String.valueOf(event.get("slug")))
                .name(String.valueOf(event.get("name")))
                .role(String.valueOf(event.getOrDefault("role", "Mentor")))
                .company(String.valueOf(event.getOrDefault("company", "")))
                .trackLabel(String.valueOf(event.getOrDefault("trackLabel", "")))
                .bio(String.valueOf(event.getOrDefault("bio", "")))
                .avatarUrl(String.valueOf(event.getOrDefault("avatarUrl", "")))
                .rating(0.0)
                .reviewsCount(0)
                .learnersCount("0")
                .sessionsCount(0)
                .yearsExp(0)
                .available(true)
                .build();
        mentorRepository.save(mentor);
    }

    private Long toLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }
}
