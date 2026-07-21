package com.lms.mentor.repository;

import com.lms.mentor.model.MentorStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentorStudentRepository extends JpaRepository<MentorStudent, Long> {
    List<MentorStudent> findByMentorUserId(Long mentorUserId);

    Optional<MentorStudent> findByMentorUserIdAndStudentId(Long mentorUserId, Long studentId);

    Optional<MentorStudent> findByMentorUserIdAndStudentIdAndCourseId(
            Long mentorUserId, Long studentId, Long courseId);

    long countByMentorUserIdAndCourseId(Long mentorUserId, Long courseId);

    @Query("select count(distinct m.studentId) from MentorStudent m where m.mentorUserId = :mentorUserId")
    long countDistinctStudentsByMentorUserId(@Param("mentorUserId") Long mentorUserId);
}
