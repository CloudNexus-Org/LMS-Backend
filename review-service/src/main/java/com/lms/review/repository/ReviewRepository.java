package com.lms.review.repository;

import com.lms.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Review> findByCourseIdAndUserId(Long courseId, Long userId);
    long countByCourseId(Long courseId);
    List<Review> findByCourseIdIn(List<Long> courseIds);
}
