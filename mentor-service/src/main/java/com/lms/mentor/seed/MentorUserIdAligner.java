package com.lms.mentor.seed;

import com.lms.mentor.repository.MentorRepository;
import com.lms.mentor.repository.MentorStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Aligns mentor user_id values with auth/user-service IDs for seeded accounts.
 * Safe to run on every startup — only updates known legacy mismatches.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class MentorUserIdAligner implements CommandLineRunner {

    private static final Map<String, Long> SLUG_TO_USER_ID = Map.of(
            "arjan-singh", 2L,
            "priya-mehta", 6L
    );
    private static final long LEGACY_ARJAN_USER_ID = 101L;
    private static final long LEGACY_PRIYA_USER_ID = 102L;

    private final MentorRepository mentorRepository;
    private final MentorStudentRepository mentorStudentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        SLUG_TO_USER_ID.forEach((slug, userId) ->
                mentorRepository.findBySlug(slug).ifPresent(mentor -> {
                    if (!userId.equals(mentor.getUserId())) {
                        log.info("Aligning mentor {} userId {} -> {}", slug, mentor.getUserId(), userId);
                        mentor.setUserId(userId);
                        mentorRepository.save(mentor);
                    }
                })
        );

        for (long legacyId : new long[] { LEGACY_ARJAN_USER_ID, LEGACY_PRIYA_USER_ID }) {
            mentorStudentRepository.findByMentorUserId(legacyId).forEach(student -> {
                long targetId = legacyId == LEGACY_ARJAN_USER_ID ? 2L : 6L;
                log.info("Aligning mentor student {} mentorUserId {} -> {}", student.getStudentId(), legacyId, targetId);
                student.setMentorUserId(targetId);
                mentorStudentRepository.save(student);
            });
        }
    }
}
