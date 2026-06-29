package com.lms.admin.repository;

import com.lms.admin.model.CourseApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseApprovalRepository extends JpaRepository<CourseApproval, String> {

    List<CourseApproval> findByStatusIgnoreCaseOrderBySubmittedAtDesc(String status);
}
