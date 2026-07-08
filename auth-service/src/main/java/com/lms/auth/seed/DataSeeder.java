package com.lms.auth.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Auth credentials are created via signup and admin provisioning — no static seed users.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Intentionally empty — credentials are provisioned dynamically.
    }
}
