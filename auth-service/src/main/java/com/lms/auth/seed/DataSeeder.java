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

        // Admin User credential (ID aligned with user-service seed data)
        seedUser(1L, "admin@realm.learn", "Password123!", UserRole.ADMIN, "Admin User");
    }

    private void seedUser(Long id, String email, String password, UserRole role, String fullName) {
        credentialRepository.save(AuthCredential.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .active(true)
                .build());
    }
}
