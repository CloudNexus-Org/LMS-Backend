package com.lms.content.repository;

import com.lms.content.model.LessonTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonTranscriptRepository extends JpaRepository<LessonTranscript, Long> {
}
