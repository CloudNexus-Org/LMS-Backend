package com.lms.user.seed;

import com.lms.user.model.User;
import com.lms.user.model.UserSettings;
import com.lms.user.model.UserStatus;
import com.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.saveAll(List.of(
                user(1L, "alex.chen@example.com", "Alex Chen", "STUDENT", UserStatus.ACTIVE,
                        date(2026, 3, 12), hoursAgo(2)),
                user(2L, "arjan@cloudnexus.com", "Dr. Arjan Singh", "MENTOR", UserStatus.ACTIVE,
                        date(2024, 1, 5), minutesAgo(30),
                        "arjan", "Staff Software Engineer", "Ex-Google", "Full-Stack Web",
                        "San Francisco, CA", "10+ years building distributed systems."),
                user(3L, "sarah.m@example.com", "Sarah Miller", "STUDENT", UserStatus.ACTIVE,
                        date(2026, 2, 18), hoursAgo(5)),
                user(4L, "admin@cloudnexus.com", "Admin User", "ADMIN", UserStatus.ACTIVE,
                        date(2023, 12, 1), hoursAgo(1)),
                user(5L, "j.wilson@example.com", "James Wilson", "STUDENT", UserStatus.INACTIVE,
                        date(2026, 1, 22), daysAgo(3)),
                user(6L, "priya.n@cloudnexus.com", "Priya Nair", "MENTOR", UserStatus.ACTIVE,
                        date(2025, 3, 3), daysAgo(1),
                        "priya.nair", "Principal ML Engineer", "Ex-Meta", "AI / ML",
                        "New York, NY", "Led ML platform teams at Meta and Netflix."),
                user(7L, "fake1234@spam.com", "Spam Account", "STUDENT", UserStatus.BANNED,
                        date(2026, 5, 5), null),
                user(8L, "emily.d@example.com", "Emily Davis", "STUDENT", UserStatus.ACTIVE,
                        date(2026, 4, 10), hoursAgo(4))
        ));
    }

    private User user(Long id, String email, String fullName, String role, UserStatus status,
                      Instant joinedAt, Instant lastActive) {
        return user(id, email, fullName, role, status, joinedAt, lastActive,
                null, null, null, null, null, null);
    }

    private User user(Long id, String email, String fullName, String role, UserStatus status,
                      Instant joinedAt, Instant lastActive,
                      String username, String professionalRole, String company,
                      String trackLabel, String location, String bio) {
        User user = User.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .role(role)
                .status(status)
                .joinedAt(joinedAt)
                .lastActive(lastActive)
                .username(username)
                .professionalRole(professionalRole)
                .company(company)
                .trackLabel(trackLabel)
                .location(location)
                .bio(bio)
                .build();
        user.setSettings(UserSettings.builder().user(user).build());
        return user;
    }

    private Instant date(int year, int month, int day) {
        return LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant hoursAgo(long hours) {
        return Instant.now().minusSeconds(hours * 3600);
    }

    private Instant minutesAgo(long minutes) {
        return Instant.now().minusSeconds(minutes * 60);
    }

    private Instant daysAgo(long days) {
        return Instant.now().minusSeconds(days * 86400);
    }
}
