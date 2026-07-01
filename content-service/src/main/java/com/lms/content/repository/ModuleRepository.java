package com.lms.content.repository;

import com.lms.content.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    void deleteByCourseId(Long courseId);
}
