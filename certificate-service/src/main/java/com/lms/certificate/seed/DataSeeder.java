package com.lms.certificate.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Certificates are issued only when students complete courses. */
@Component
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // no seed certificates
    }
}
