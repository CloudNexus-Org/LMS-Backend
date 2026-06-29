package com.lms.auth.repository;

import com.lms.auth.model.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthCredentialRepository extends JpaRepository<AuthCredential, Long> {

    Optional<AuthCredential> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
