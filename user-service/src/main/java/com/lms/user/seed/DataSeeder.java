package com.lms.user.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * User records are created via signup and admin flows — no static seed users.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Intentionally empty — directory is populated dynamically.
    }
}
