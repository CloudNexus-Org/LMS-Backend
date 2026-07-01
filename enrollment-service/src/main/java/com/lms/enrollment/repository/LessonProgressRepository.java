package com.lms.enrollment.repository;

import com.lms.enrollment.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<LessonProgress> findByUserIdAndTrackId(Long userId, String trackId);
    long countByUserIdAndTrackIdAndCompletedTrue(Long userId, String trackId);
    List<LessonProgress> findByUserIdAndTrackIdAndCompletedTrue(Long userId, String trackId);
}
