package com.lms.content.event;

import com.lms.content.model.CourseContent;
import com.lms.content.model.Lesson;

public interface ContentEventPublisher {
    void publishCourseSubmitted(CourseContent course);
    void publishCoursePublished(CourseContent course);
    void publishLessonCreated(Lesson lesson, Long courseId, Long mentorId);
}
