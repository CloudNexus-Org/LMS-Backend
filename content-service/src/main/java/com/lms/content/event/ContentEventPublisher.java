package com.lms.content.event;

import com.lms.content.model.CourseContent;
import com.lms.content.model.Lesson;

import java.util.Map;

public interface ContentEventPublisher {
    void publishCourseSubmitted(Map<String, Object> event);
    void publishCoursePublished(CourseContent course);
    void publishLessonCreated(Lesson lesson, Long courseId, Long mentorId);
}
