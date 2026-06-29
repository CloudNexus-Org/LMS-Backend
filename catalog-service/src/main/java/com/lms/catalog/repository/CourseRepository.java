package com.lms.catalog.repository;

import com.lms.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    Optional<Course> findBySlug(String slug);

    List<Course> findByFeaturedTrueAndStatus(String status);

    List<Course> findByStatus(String status);

    List<Course> findByExploreTypeIgnoreCaseAndStatus(String exploreType, String status);

    List<Course> findByIdIn(List<Long> ids);
}
