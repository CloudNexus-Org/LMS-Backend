package com.lms.certificate.repository;

import com.lms.certificate.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserIdOrderByIssueDateDesc(Long userId);
    Optional<Certificate> findByCode(String code);
    Optional<Certificate> findByCodeAndUserId(String code, Long userId);
    Optional<Certificate> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndTrackId(Long userId, String trackId);
}
