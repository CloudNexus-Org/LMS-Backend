package com.lms.enrollment.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Enrollment data comes from real purchases and enrollments only. */
@Component
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // no seed enrollments
    }
}
