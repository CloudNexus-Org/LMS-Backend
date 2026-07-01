package com.lms.analytics.repository;

import com.lms.analytics.model.StudentActivity;
import com.lms.analytics.model.StudentActivityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentActivityRepository extends JpaRepository<StudentActivity, StudentActivityId> {
    List<StudentActivity> findByIdUserIdAndIdDateBetweenOrderByIdDateAsc(Long userId, LocalDate from, LocalDate to);
    List<StudentActivity> findByIdUserId(Long userId);
}
