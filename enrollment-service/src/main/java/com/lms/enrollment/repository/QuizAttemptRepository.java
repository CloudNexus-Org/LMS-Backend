package com.lms.enrollment.repository;

import com.lms.enrollment.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdAndTrackIdOrderByAttemptedAtDesc(Long userId, String trackId);

    List<QuizAttempt> findByUserIdAndLessonIdOrderByAttemptedAtDesc(Long userId, Long lessonId);

    Optional<QuizAttempt> findFirstByUserIdAndLessonIdAndPassedTrueOrderByAttemptedAtDesc(Long userId, Long lessonId);

    boolean existsByUserIdAndLessonIdAndPassedTrue(Long userId, Long lessonId);
}
