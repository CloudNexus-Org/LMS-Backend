package com.lms.auth.seed;

import com.lms.auth.model.AuthCredential;
import com.lms.auth.model.UserRole;
import com.lms.auth.repository.AuthCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a bootstrap admin exists so the platform is usable on a fresh database.
 * Does not wipe or replace existing credentials.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final long ADMIN_ID = 1L;
    private static final String ADMIN_EMAIL = "admin@cloudnexus.com";
    private static final String ADMIN_PASSWORD = "Password123!";

    private final AuthCredentialRepository credentialRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void run(String... args) {
        if (credentialRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)
                || credentialRepository.existsById(ADMIN_ID)) {
            return;
        }
        credentialRepository.save(AuthCredential.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .fullName("Admin User")
                .role(UserRole.ADMIN)
                .active(true)
                .build());
        log.info("Seeded bootstrap admin credential id={} email={}", ADMIN_ID, ADMIN_EMAIL);
    }
}
