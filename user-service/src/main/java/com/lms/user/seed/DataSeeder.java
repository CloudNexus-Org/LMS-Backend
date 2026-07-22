package com.lms.user.seed;

import com.lms.user.model.User;
import com.lms.user.model.UserSettings;
import com.lms.user.model.UserStatus;
import com.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Ensures a bootstrap admin profile exists with the same id as auth-service DataSeeder.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final long ADMIN_ID = 1L;
    private static final String ADMIN_EMAIL = "admin@cloudnexus.com";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).isPresent()
                || userRepository.existsById(ADMIN_ID)) {
            return;
        }
        Instant now = Instant.now();
        User admin = User.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .fullName("Admin User")
                .role("ADMIN")
                .status(UserStatus.ACTIVE)
                .joinedAt(now)
                .lastActive(now)
                .username("admin")
                .build();
        UserSettings settings = UserSettings.builder()
                .user(admin)
                .build();
        admin.setSettings(settings);
        userRepository.save(admin);
        log.info("Seeded bootstrap admin profile id={} email={}", ADMIN_ID, ADMIN_EMAIL);
    }
}
