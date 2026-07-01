package com.lms.content.event;

import com.lms.content.model.CourseContent;
import com.lms.content.model.Lesson;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpContentEventProducer implements ContentEventPublisher {

    @Override
    public void publishCourseSubmitted(CourseContent course) {
    }

    @Override
    public void publishCoursePublished(CourseContent course) {
    }

    @Override
    public void publishLessonCreated(Lesson lesson, Long courseId, Long mentorId) {
    }
}
