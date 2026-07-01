package com.lms.content.event;

import com.lms.content.model.CourseContent;
import com.lms.content.model.Lesson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ContentEventProducer implements ContentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCourseSubmitted(CourseContent course) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contentId", course.getId());
        payload.put("courseId", course.getId());
        payload.put("mentorId", course.getMentorId());
        payload.put("title", course.getTitle());
        payload.put("category", course.getCategory());
        payload.put("level", course.getLevel());
        kafkaTemplate.send("course.submitted", String.valueOf(course.getId()), payload);
        log.info("Published course.submitted for content {}", course.getId());
    }

    @Override
    public void publishCoursePublished(CourseContent course) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contentId", course.getId());
        payload.put("courseId", course.getCourseId());
        payload.put("mentorId", course.getMentorId());
        payload.put("title", course.getTitle());
        payload.put("trackId", course.getTrackId());
        kafkaTemplate.send("course.published", String.valueOf(course.getId()), payload);
        log.info("Published course.published for content {}", course.getId());
    }

    @Override
    public void publishLessonCreated(Lesson lesson, Long courseId, Long mentorId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("lessonId", lesson.getId());
        payload.put("courseId", courseId);
        payload.put("mentorId", mentorId);
        payload.put("title", lesson.getTitle());
        payload.put("type", lesson.getType());
        kafkaTemplate.send("lesson.created", String.valueOf(lesson.getId()), payload);
    }
}
