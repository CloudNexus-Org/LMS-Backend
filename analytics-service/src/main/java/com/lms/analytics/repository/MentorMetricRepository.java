package com.lms.analytics.repository;

import com.lms.analytics.model.MentorMetric;
import com.lms.analytics.model.MentorMetricId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MentorMetricRepository extends JpaRepository<MentorMetric, MentorMetricId> {
    List<MentorMetric> findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(Long mentorId, LocalDate from, LocalDate to);
    List<MentorMetric> findByIdMentorId(Long mentorId);
}
