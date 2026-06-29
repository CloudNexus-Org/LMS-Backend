package com.lms.auth.repository;

import com.lms.auth.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByEmailIgnoreCaseAndPurposeAndUsedFalseOrderByExpiresAtDesc(
            String email, String purpose);

    Optional<OtpCode> findTopByEmailIgnoreCaseAndCodeAndPurposeAndUsedFalseOrderByExpiresAtDesc(
            String email, String code, String purpose);
}
