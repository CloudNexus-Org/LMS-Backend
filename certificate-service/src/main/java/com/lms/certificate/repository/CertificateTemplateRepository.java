package com.lms.certificate.repository;

import com.lms.certificate.model.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    Optional<CertificateTemplate> findByTrackId(String trackId);
}
