package com.lms.learning.repository;

import com.lms.learning.model.LessonQa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonQaRepository extends JpaRepository<LessonQa, Long> {
    List<LessonQa> findByLessonIdOrderByCreatedAtDesc(Long lessonId);
}
