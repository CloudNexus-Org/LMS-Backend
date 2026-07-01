package com.lms.content.repository;

import com.lms.content.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);
    List<Lesson> findByModuleIdInOrderByOrderIndexAsc(Collection<Long> moduleIds);
    void deleteByModuleId(Long moduleId);
}
