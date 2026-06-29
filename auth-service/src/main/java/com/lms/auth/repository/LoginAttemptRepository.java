package com.lms.auth.repository;

import com.lms.auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    long countByEmailIgnoreCaseAndSuccessFalseAndAttemptedAtAfter(String email, Instant since);
}
