package com.lms.catalog.repository;

import com.lms.catalog.model.TrackCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackCourseRepository extends JpaRepository<TrackCourse, Long> {
    void deleteByCourseId(Long courseId);
}
