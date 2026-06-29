package com.lms.mentor.repository;

import com.lms.mentor.model.MentorStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorStudentRepository extends JpaRepository<MentorStudent, Long> {
    List<MentorStudent> findByMentorUserId(Long mentorUserId);
    Optional<MentorStudent> findByMentorUserIdAndStudentId(Long mentorUserId, Long studentId);
}
