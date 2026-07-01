package com.lms.analytics.repository;

import com.lms.analytics.model.DailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyMetricRepository extends JpaRepository<DailyMetric, LocalDate> {
    List<DailyMetric> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
