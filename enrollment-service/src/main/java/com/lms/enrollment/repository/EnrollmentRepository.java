package com.lms.enrollment.repository;

import com.lms.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);
    Optional<Enrollment> findByUserIdAndTrackId(Long userId, String trackId);
    boolean existsByUserIdAndTrackId(Long userId, String trackId);
    long countByUserIdAndStatus(Long userId, String status);
}
