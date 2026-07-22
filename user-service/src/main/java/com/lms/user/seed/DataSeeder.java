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
                                user(1L, "admin@realm.learn", "Admin User", "ADMIN", UserStatus.ACTIVE,
                                                date(2023, 12, 1), hoursAgo(1))));
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
