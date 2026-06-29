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
        if (mentorRepository.count() > 0) return;

        Mentor arjan = mentor("arjan-singh", 101L, "Arjan Singh", "Staff Software Engineer", "Ex-Google",
                "Full-Stack Web",
                "10+ years building distributed systems at Google and Stripe.",
                "Arjan spent a decade at Google's Core Infrastructure team before joining Stripe.",
                "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=400",
                4.9, 312, "4.2k", 580, 11, "San Francisco, CA", true,
                List.of("System Design", "Distributed Systems", "Go", "Kubernetes", "SRE"),
                List.of("Google Distinguished Engineer nominee 2022",
                        "Open-sourced Raft consensus library with 3k+ GitHub stars"),
                List.of(exp("Staff Software Engineer", "Google", "2015 – 2023",
                                "Led multi-region replication framework for Google Cloud Storage."),
                        exp("Senior Software Engineer", "Stripe", "2013 – 2015",
                                "Owned payments reliability roadmap and gRPC migration.")),
                List.of(taught("System Design for Senior Engineers", "Advanced", 12, 18, 1L),
                        taught("Production Kubernetes: Zero to Hero", "Intermediate", 9, 14, 7L)));

        Mentor priya = mentor("priya-mehta", 102L, "Priya Mehta", "Principal ML Engineer", "Ex-Meta",
                "AI / ML",
                "Led ML platform teams at Meta and Netflix.",
                "Priya has spent over 8 years at the intersection of ML research and production engineering.",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400",
                4.95, 497, "6.8k", 720, 9, "New York, NY", true,
                List.of("PyTorch", "LLMs", "Feature Engineering", "MLOps", "Python"),
                List.of("Patent holder — real-time feature freshness monitoring",
                        "NeurIPS 2022 workshop paper on low-latency inference"),
                List.of(exp("Principal ML Engineer", "Meta", "2018 – 2024",
                                "Designed ranking model refresh pipeline for Reels."),
                        exp("Senior ML Engineer", "Netflix", "2015 – 2018",
                                "Built real-time feature store for 200+ production ML models.")),
                List.of(taught("LLMs in Production", "Advanced", 10, 16, 2L),
                        taught("MLOps: From Notebook to Prod", "Intermediate", 8, 12, 5L)));

        mentorRepository.saveAll(List.of(arjan, priya));

        mentorStudentRepository.saveAll(List.of(
                MentorStudent.builder().mentorUserId(101L).studentId(201L)
                        .studentName("Aarav Sharma").studentEmail("aarav@example.com")
                        .courseId(1L).courseTitle("AWS Solution Architect")
                        .progress(65).status("in-progress").build(),
                MentorStudent.builder().mentorUserId(101L).studentId(202L)
                        .studentName("Sneha Patel").studentEmail("sneha@example.com")
                        .courseId(7L).courseTitle("Docker Containerization Essentials")
                        .progress(82).status("in-progress").build()
        ));
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
