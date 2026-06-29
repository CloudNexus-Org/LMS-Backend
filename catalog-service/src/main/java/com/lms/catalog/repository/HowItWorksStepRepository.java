package com.lms.catalog.repository;

import com.lms.catalog.model.HowItWorksStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HowItWorksStepRepository extends JpaRepository<HowItWorksStep, Long> {

    List<HowItWorksStep> findAllByOrderByOrderIndexAsc();
}
