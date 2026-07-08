package com.lms.analytics.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Metrics are recorded from live platform events — no static seed data.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Intentionally empty.
    }
}
