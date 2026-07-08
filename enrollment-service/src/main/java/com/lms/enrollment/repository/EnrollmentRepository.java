package com.lms.enrollment.repository;

import com.lms.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);
    Optional<Enrollment> findByUserIdAndTrackId(Long userId, String trackId);
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndTrackId(Long userId, String trackId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    long countByUserIdAndStatus(Long userId, String status);
}
