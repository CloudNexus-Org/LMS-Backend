package com.lms.mentor.seed;

import com.lms.mentor.model.*;
import com.lms.mentor.repository.MentorRepository;
import com.lms.mentor.repository.MentorStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MentorRepository mentorRepository;
    private final MentorStudentRepository mentorStudentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Static hardcoded mentors removed
    }

    private Mentor mentor(String slug, Long userId, String name, String role, String company, String trackLabel,
                          String bio, String longBio, String avatar, double rating, int reviews, String learners,
                          int sessions, int yearsExp, String location, boolean available,
                          List<String> specialties, List<String> achievements,
                          List<MentorExperience> experiences, List<MentorTaughtCourse> taught) {
        Mentor mentor = Mentor.builder()
                .slug(slug).userId(userId).name(name).role(role).company(company)
                .trackLabel(trackLabel).bio(bio).longBio(longBio).avatarUrl(avatar)
                .rating(rating).reviewsCount(reviews).learnersCount(learners)
                .sessionsCount(sessions).yearsExp(yearsExp).location(location).available(available)
                .specialties(specialties).achievements(achievements)
                .build();
        experiences.forEach(e -> { e.setMentor(mentor); mentor.getExperience().add(e); });
        taught.forEach(t -> { t.setMentor(mentor); mentor.getTaughtCourses().add(t); });
        return mentor;
    }

    private MentorExperience exp(String title, String org, String period, String description) {
        return MentorExperience.builder().title(title).org(org).period(period).description(description).build();
    }

    private MentorTaughtCourse taught(String title, String level, int modules, int hours, Long courseId) {
        return MentorTaughtCourse.builder()
                .title(title).level(level).modules(modules).hours(hours).courseId(courseId).build();
    }
}
