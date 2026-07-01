package com.lms.analytics.repository;

import com.lms.analytics.model.CourseMetric;
import com.lms.analytics.model.CourseMetricId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CourseMetricRepository extends JpaRepository<CourseMetric, CourseMetricId> {
    List<CourseMetric> findByIdCourseIdAndIdDateBetweenOrderByIdDateAsc(Long courseId, LocalDate from, LocalDate to);
    List<CourseMetric> findByIdDateBetweenOrderByIdDateAsc(LocalDate from, LocalDate to);
}
