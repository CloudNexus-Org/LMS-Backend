package com.lms.user.seed;

import com.lms.user.model.User;
import com.lms.user.model.UserStatus;
import com.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

//@Component
@RequiredArgsConstructor
@Slf4j
public class UserDirectoryCleanup implements CommandLineRunner {

    private static final Set<String> ALLOWED_EMAILS = Set.of(
            "admin@realm.learn",
            "ronakdhanotiya123@gmail.com",
            "raunakdhanotiyagenai@gmail.com"
    );

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int removed = 0;
        for (User user : userRepository.findAll()) {
            String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase(Locale.ROOT) : "";
            if (!ALLOWED_EMAILS.contains(email)) {
                if (user.getStatus() != UserStatus.DELETED) {
                    user.setStatus(UserStatus.DELETED);
                    user.setLastActive(Instant.now());
                    userRepository.save(user);
                    removed++;
                }
                continue;
            }
            normalizeKeptUser(user, email);
            userRepository.save(user);
        }
        if (removed > 0) {
            log.info("User directory cleanup: marked {} accounts as deleted", removed);
        }
    }

    private void normalizeKeptUser(User user, String email) {
        if ("admin@realm.learn".equals(email)) {
            user.setFullName("Admin");
            user.setRole("ADMIN");
            user.setStatus(UserStatus.ACTIVE);
            return;
        }
        if ("ronakdhanotiya123@gmail.com".equals(email)) {
            user.setFullName("Raunak");
            user.setRole("STUDENT");
            user.setStatus(UserStatus.ACTIVE);
            return;
        }
        if ("raunakdhanotiyagenai@gmail.com".equals(email)) {
            user.setFullName("Raunak Dhanotiya");
            user.setRole("MENTOR");
            user.setStatus(UserStatus.ACTIVE);
        }
    }
}
