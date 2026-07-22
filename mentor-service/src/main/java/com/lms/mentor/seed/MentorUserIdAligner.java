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

    private static final Map<String, Long> SLUG_TO_USER_ID = Map.of();

    private final MentorRepository mentorRepository;
    private final MentorStudentRepository mentorStudentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Legacy static alignments removed
    }
}
