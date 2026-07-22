package com.lms.mentor.event;

import com.lms.mentor.model.Mentor;
import com.lms.mentor.model.MentorStudent;
import com.lms.mentor.repository.MentorRepository;
import com.lms.mentor.repository.MentorStudentRepository;
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
    private final MentorStudentRepository mentorStudentRepository;

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
                .location(String.valueOf(event.getOrDefault("location", "")))
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

    @KafkaListener(topics = "enrollment.created", groupId = "mentor-service")
    @Transactional
    public void onEnrollmentCreated(Map<String, Object> event) {
        Long mentorId = toLong(event.get("mentorId"));
        Long studentId = toLong(event.get("userId"));
        Long courseId = toLong(event.get("courseId"));
        if (mentorId == null || studentId == null) {
            log.debug("Skipping enrollment.created for mentor roster — missing mentorId/userId: {}", event);
            return;
        }
        if (courseId != null && courseId <= 0) {
            courseId = null;
        }

        if (courseId != null) {
            boolean exists = mentorStudentRepository
                    .findByMentorUserIdAndStudentIdAndCourseId(mentorId, studentId, courseId)
                    .isPresent();
            if (exists) {
                return;
            }
        } else if (mentorStudentRepository.findByMentorUserIdAndStudentId(mentorId, studentId).isPresent()) {
            return;
        }

        String studentName = stringVal(event.get("studentName"));
        if (studentName == null || studentName.isBlank()) {
            studentName = "Student #" + studentId;
        }
        String courseTitle = stringVal(event.get("courseTitle"));
        if (courseTitle == null || courseTitle.isBlank()) {
            courseTitle = stringVal(event.get("trackId"));
        }

        mentorStudentRepository.save(MentorStudent.builder()
                .mentorUserId(mentorId)
                .studentId(studentId)
                .studentName(studentName)
                .studentEmail("")
                .courseId(courseId)
                .courseTitle(courseTitle != null ? courseTitle : "")
                .progress(0)
                .status("ACTIVE")
                .build());

        long distinct = mentorStudentRepository.countDistinctStudentsByMentorUserId(mentorId);
        mentorRepository.findByUserId(mentorId).ifPresent(mentor -> {
            mentor.setLearnersCount(String.valueOf(distinct));
            mentorRepository.save(mentor);
        });
        log.info("Linked student {} to mentor {} for course {}", studentId, mentorId, courseId);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString() : null;
    }
}
