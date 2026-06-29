package com.lms.admin.repository;

import com.lms.admin.model.MentorPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorPayoutRepository extends JpaRepository<MentorPayout, String> {

    List<MentorPayout> findAllByOrderByProcessedAtDesc();
}
