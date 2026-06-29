package com.lms.auth.seed;

import com.lms.auth.model.AuthCredential;
import com.lms.auth.model.UserRole;
import com.lms.auth.repository.AuthCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AuthCredentialRepository credentialRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void run(String... args) {
        if (credentialRepository.count() > 0) {
            return;
        }

        seedUser("admin@cloudnexus.com", "Password123!", UserRole.ADMIN, "Admin User");
        seedUser("arjan@cloudnexus.com", "Password123!", UserRole.MENTOR, "Dr. Arjan Singh");
        seedUser("alex.chen@example.com", "Password123!", UserRole.STUDENT, "Alex Chen");
        seedUser("sarah.m@example.com", "Password123!", UserRole.STUDENT, "Sarah Miller");
    }

    private void seedUser(String email, String password, UserRole role, String fullName) {
        credentialRepository.save(AuthCredential.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .active(true)
                .build());
    }
}
