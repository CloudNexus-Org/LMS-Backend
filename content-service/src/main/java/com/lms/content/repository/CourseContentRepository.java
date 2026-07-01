package com.lms.content.repository;

import com.lms.content.model.CourseContent;
import com.lms.content.model.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByMentorIdOrderByUpdatedAtDesc(Long mentorId);
    List<CourseContent> findByMentorIdAndStatusOrderByUpdatedAtDesc(Long mentorId, CourseStatus status);
    List<CourseContent> findByTrackId(String trackId);
}
