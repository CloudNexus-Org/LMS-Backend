package com.lms.auth.seed;

import com.lms.auth.model.AuthCredential;
import com.lms.auth.repository.AuthCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Destructive allowlist cleanup — OFF by default.
 * Enable only with {@code lms.directory.cleanup.enabled=true} (never in normal local/docker runs).
 */
@Component
@ConditionalOnProperty(name = "lms.directory.cleanup.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AuthDirectoryCleanup implements CommandLineRunner {

    private static final Set<String> ALLOWED_EMAILS = Set.of(
            "admin@cloudnexus.com",
            "ronakdhanotiya123@gmail.com",
            "raunakdhanotiyagenai@gmail.com"
    );

    private final AuthCredentialRepository credentialRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<AuthCredential> toRemove = new ArrayList<>();
        for (AuthCredential credential : credentialRepository.findAll()) {
            String email = credential.getEmail() != null
                    ? credential.getEmail().trim().toLowerCase(Locale.ROOT)
                    : "";
            if (!ALLOWED_EMAILS.contains(email)) {
                toRemove.add(credential);
            }
        }
        if (!toRemove.isEmpty()) {
            credentialRepository.deleteAll(toRemove);
            log.info("Auth directory cleanup: removed {} credential(s)", toRemove.size());
        }
    }
}
