package com.lms.learning.repository;

import com.lms.learning.model.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    List<LearningSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<LearningSession> findByIdAndUserId(Long id, Long userId);
}
