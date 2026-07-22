package com.lms.mentor.service;

import com.lms.mentor.client.UserClient;
import com.lms.mentor.model.Mentor;
import com.lms.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentorProvisioner {

    private final MentorRepository mentorRepository;
    private final UserClient userClient;

    private static final Pattern SLUG_SAFE = Pattern.compile("[^a-z0-9]+");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mentor ensureMentor(Long userId) {
        return mentorRepository.findByUserId(userId)
                .orElseGet(() -> createMentorForUser(userId));
    }

    private Mentor createMentorForUser(Long userId) {
        UserClient.UserSnapshot user = userClient.findUser(userId).orElse(null);
        String name = user != null && user.fullName() != null && !user.fullName().isBlank()
                ? user.fullName()
                : "Mentor " + userId;
        String baseSlug = slugify(name);
        String slug = uniqueSlug(baseSlug, userId);

        Mentor mentor = Mentor.builder()
                .userId(userId)
                .slug(slug)
                .name(name)
                .role("Mentor")
                .company("")
                .trackLabel("")
                .bio("Realm mentor")
                .longBio("")
                .avatarUrl("")
                .rating(0.0)
                .reviewsCount(0)
                .learnersCount("0")
                .sessionsCount(0)
                .yearsExp(0)
                .location("")
                .available(true)
                .build();
        return mentorRepository.save(mentor);
    }

    private String slugify(String raw) {
        String slug = SLUG_SAFE.matcher(raw.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "mentor" : slug;
    }

    private String uniqueSlug(String base, Long userId) {
        String candidate = base;
        int attempt = 0;
        while (mentorRepository.findBySlug(candidate).isPresent()) {
            attempt += 1;
            candidate = base + "-" + (attempt == 1 ? userId : userId + "-" + attempt);
        }
        return candidate;
    }
}
