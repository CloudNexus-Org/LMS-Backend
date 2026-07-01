package com.lms.content.seed;

import com.lms.content.model.*;
import com.lms.content.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseContentRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository resourceRepository;
    private final LessonTranscriptRepository transcriptRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) return;

        CourseContent cloud = courseRepository.save(CourseContent.builder()
                .mentorId(2L)
                .status(CourseStatus.PUBLISHED)
                .courseId(1L)
                .title("Cloud Architecture Patterns")
                .subtitle("Design resilient systems on AWS, Azure & GCP")
                .description("Learn production-grade cloud architecture from engineers who built hyperscale systems.")
                .category("Cloud & DevOps")
                .level("Intermediate")
                .language("English")
                .trackId("cloud")
                .pricingPlan("paid")
                .price(24999.0)
                .outcomesJson("[\"Design multi-region architectures\",\"Master IAM and VPC networking\",\"Optimize cloud costs\"]")
                .tagsJson("[\"AWS\",\"Azure\",\"GCP\"]")
                .build());

        CourseModule mod1 = moduleRepository.save(CourseModule.builder()
                .courseId(cloud.getId())
                .title("Introduction to Cloud Architecture")
                .orderIndex(0)
                .description("Foundations and core patterns")
                .build());

        Lesson l1 = lessonRepository.save(Lesson.builder()
                .moduleId(mod1.getId())
                .title("What is Cloud Architecture?")
                .type("video")
                .durationMin(8)
                .orderIndex(0)
                .contentUrl("/videos/how-it-works.mp4")
                .previewFree(true)
                .summary("Overview of cloud-native design principles and the CAP theorem.")
                .build());

        lessonRepository.save(Lesson.builder()
                .moduleId(mod1.getId())
                .title("Multi-Region Design Patterns")
                .type("reading")
                .durationMin(12)
                .orderIndex(1)
                .readingContent("## Multi-Region Design\n\nActive-active and active-passive patterns for global availability.")
                .previewFree(false)
                .summary("Explore failover strategies and data replication models.")
                .build());

        resourceRepository.save(LessonResource.builder()
                .lessonId(l1.getId())
                .title("Architecture Diagram Template")
                .fileUrl("/resources/cloud-architecture-template.pdf")
                .fileType("pdf")
                .build());

        transcriptRepository.save(LessonTranscript.builder()
                .lessonId(l1.getId())
                .language("en")
                .transcriptText("""
                        Welcome to Cloud Architecture Patterns.
                        In this lesson we explore why cloud-native design matters.
                        We'll cover availability zones, regions, and fault domains.
                        """)
                .build());

        CourseContent draft = courseRepository.save(CourseContent.builder()
                .mentorId(2L)
                .status(CourseStatus.DRAFT)
                .title("Enterprise React Systems")
                .category("Frontend Engineering")
                .level("Advanced")
                .language("English")
                .trackId("frontend")
                .pricingPlan("paid")
                .price(19999.0)
                .build());

        CourseModule draftMod = moduleRepository.save(CourseModule.builder()
                .courseId(draft.getId())
                .title("Module 1 — Advanced Hooks")
                .orderIndex(0)
                .build());

        lessonRepository.save(Lesson.builder()
                .moduleId(draftMod.getId())
                .title("Custom Hooks Deep Dive")
                .type("video")
                .durationMin(15)
                .orderIndex(0)
                .previewFree(true)
                .build());

        courseRepository.save(CourseContent.builder()
                .mentorId(2L)
                .status(CourseStatus.APPROVED)
                .title("Rust for Frontend Devs")
                .category("Frontend Engineering")
                .level("Intermediate")
                .language("English")
                .trackId("frontend")
                .pricingPlan("premium")
                .price(29999.0)
                .build());
    }
}
