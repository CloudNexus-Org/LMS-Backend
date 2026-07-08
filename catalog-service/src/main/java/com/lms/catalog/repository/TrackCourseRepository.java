package com.lms.catalog.repository;

import com.lms.catalog.model.TrackCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackCourseRepository extends JpaRepository<TrackCourse, Long> {
    void deleteByCourseId(Long courseId);

    Optional<TrackCourse> findFirstByCourseIdOrderByOrderIndexAsc(Long courseId);
}
