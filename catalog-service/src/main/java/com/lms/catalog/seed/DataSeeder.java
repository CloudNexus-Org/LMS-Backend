package com.lms.catalog.seed;

import com.lms.catalog.model.*;
import com.lms.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final TrackRepository trackRepository;
    private final CategoryRepository categoryRepository;
    private final FaqRepository faqRepository;
    private final TestimonialRepository testimonialRepository;
    private final HowItWorksStepRepository howItWorksStepRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            return;
        }
        seedCourses();
        seedTracks();
        seedCategories();
        seedFaq();
        seedTestimonials();
        seedHowItWorks();
    }

    private void seedCourses() {
        courseRepository.saveAll(List.of(
                course("aws-solution-architect", "AWS Solution Architect", "Dr. Arjan Singh", 1L,
                        "Master EC2, S3, and Lambda to build highly scalable and fault-tolerant cloud infrastructures.",
                        4.8, 2840, "15.4k", "Intermediate", "24 Hours", 12, 42,
                        2999, 5999, "cloud", true,
                        List.of("Design highly available, multi-region architectures",
                                "Master EC2, S3, Lambda, RDS, and VPC fundamentals",
                                "Pass the AWS Solution Architect Associate exam"),
                        List.of("AWS", "EC2", "S3", "Lambda")),
                course("azure-generative-ai", "Azure Generative AI Services", "Sarah Jenkins", 2L,
                        "Dive deep into generative models, neural networks, and machine learning on Azure.",
                        4.9, 1120, "5k", "Advanced", "18 Hours", 9, 32,
                        3499, 6999, "ai", true,
                        List.of("Fine-tune LLMs on Azure OpenAI Service",
                                "Build production-ready RAG pipelines",
                                "Deploy and monitor AI apps at scale"),
                        List.of("Azure", "OpenAI", "RAG", "LLM")),
                course("modern-javascript", "Modern JavaScript", "Prof. David Miller", 3L,
                        "Build robust frontend applications with ES6+, async patterns, and scalable designs.",
                        4.7, 3560, "12k", "Beginner", "30 Hours", 15, 48,
                        1999, 3999, "fullstack", true,
                        List.of("Master ES6+, async/await, and modules",
                                "Write tested, maintainable JavaScript",
                                "Architect scalable frontend applications"),
                        List.of("JavaScript", "ES6", "Frontend")),
                course("high-performance-go", "High Performance Go (Golang)", "Ken Thompson Jr.", 4L,
                        "Learn concurrency patterns and build lightning-fast microservices using Go.",
                        4.6, 2180, "17k", "Intermediate", "15 Hours", 8, 28,
                        2499, 4999, "backend", false,
                        List.of("Master goroutines, channels, and the Go runtime",
                                "Build production microservices with gRPC",
                                "Profile and optimize for low-latency systems"),
                        List.of("Go", "gRPC", "Microservices")),
                course("python-data-engineering", "Python for Data Engineering", "Dr. Angela Yu", 5L,
                        "Automate data pipelines and process large-scale datasets using Python.",
                        4.8, 4210, "15k", "Beginner", "40 Hours", 20, 56,
                        1999, 4499, "data", true,
                        List.of("Build robust ETL pipelines with Pandas and Airflow",
                                "Process large datasets with PySpark",
                                "Ship data products to production"),
                        List.of("Python", "Airflow", "PySpark")),
                course("gcp-cloud-engineering", "GCP Cloud Engineering", "Michael Chang", 6L,
                        "Leverage Google Cloud Platform for big data, networking, and HPC solutions.",
                        4.5, 1890, "25k", "Intermediate", "20 Hours", 10, 36,
                        2799, 5499, "cloud", false,
                        List.of("Architect GKE, BigQuery, and Pub/Sub workloads",
                                "Implement IAM and VPC networking on GCP",
                                "Pass the GCP Associate Cloud Engineer exam"),
                        List.of("GCP", "GKE", "BigQuery")),
                course("docker-containerization", "Docker Containerization Essentials", "James Wilson", 7L,
                        "Package applications consistently and optimize CI/CD pipelines with Docker.",
                        4.9, 5120, "28k", "Intermediate", "10 Hours", 6, 22,
                        1799, 3599, "devops", true,
                        List.of("Write production-ready Dockerfiles",
                                "Master Docker Compose and multi-stage builds",
                                "Integrate Docker into CI/CD pipelines"),
                        List.of("Docker", "CI/CD", "Containers")),
                course("kubernetes-production", "Kubernetes Production Mastery", "Prof. Elena Rodriguez", 8L,
                        "Deploy and manage large-scale container clusters with self-healing and scaling.",
                        4.9, 2640, "12k", "Advanced", "28 Hours", 14, 44,
                        3299, 6499, "devops", false,
                        List.of("Deploy and operate production K8s clusters",
                                "Master Helm, Operators, and service mesh",
                                "Run incident-response and SRE playbooks"),
                        List.of("Kubernetes", "Helm", "SRE"))
        ));
    }

    private Course course(String slug, String title, String professor, Long mentorId,
                          String description, double rating, int reviews, String enrolled,
                          String difficulty, String duration, int modules, int lessons,
                          int price, int originalPrice, String exploreType, boolean featured,
                          List<String> outcomes, List<String> skills) {
        Course course = Course.builder()
                .slug(slug)
                .title(title)
                .professor(professor)
                .mentorId(mentorId)
                .description(description)
                .rating(rating)
                .reviewCount(reviews)
                .enrolled(enrolled)
                .difficulty(difficulty)
                .duration(duration)
                .modules(modules)
                .lessons(lessons)
                .price(BigDecimal.valueOf(price))
                .originalPrice(BigDecimal.valueOf(originalPrice))
                .exploreType(exploreType)
                .featured(featured)
                .freePreview(true)
                .status("PUBLISHED")
                .thumbnailUrl("https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800")
                .outcomes(outcomes)
                .skills(skills)
                .build();
        return course;
    }

    private void seedTracks() {
        Long awsId = courseRepository.findBySlug("aws-solution-architect").map(Course::getId).orElse(1L);
        Long azureId = courseRepository.findBySlug("azure-generative-ai").map(Course::getId).orElse(2L);
        Long gcpId = courseRepository.findBySlug("gcp-cloud-engineering").map(Course::getId).orElse(6L);
        Long pythonId = courseRepository.findBySlug("python-data-engineering").map(Course::getId).orElse(5L);

        Track cloud = track("cloud", "Cloud Engineer",
                "Master AWS, Azure & GCP with engineers who built them.",
                "A 16-week intensive from cloud fundamentals to production-grade multi-region architectures.",
                24999, 32999, 16, "8–10", "Intermediate to Advanced", "arjan-singh",
                "primary", "cloud", "₹22 LPA", "$135k", 142, "8,400+", 4.8, 1240,
                "Most popular", "Mon, 18 May 2026", "English", "Verified Cloud Engineer Certificate",
                List.of(awsId, azureId, gcpId),
                List.of("AWS", "Azure", "GCP", "Terraform", "Kubernetes"),
                List.of("Design highly available, multi-region cloud architectures",
                        "Master IAM, VPC, and zero-trust networking patterns"));

        Track ai = track("ai", "AI / ML Engineer",
                "Build production ML systems and LLM apps with ex-Meta engineers.",
                "From PyTorch fundamentals to deploying LLM-powered products in production.",
                29999, 39999, 14, "10–12", "Intermediate to Advanced", "priya-mehta",
                "violet", "sparkles", "₹28 LPA", "$155k", 98, "5,200+", 4.9, 890,
                "Trending", "Mon, 25 May 2026", "English", "Verified AI Engineer Certificate",
                List.of(azureId, pythonId),
                List.of("PyTorch", "LLMs", "MLOps", "Python"),
                List.of("Fine-tune and deploy LLMs in production",
                        "Build real-time feature stores and inference pipelines"));

        trackRepository.saveAll(List.of(cloud, ai));
    }

    private Track track(String id, String name, String tagline, String description,
                        int price, int originalPrice, int weeks, String hoursPerWeek, String level,
                        String leadMentorSlug, String color, String iconKey, String salary,
                        String medianSalary, int activeLearners, String enrolled, double rating,
                        int reviews, String badge, String nextCohort, String language, String certificate,
                        List<Long> courseIds, List<String> skills, List<String> outcomes) {
        Track track = Track.builder()
                .id(id)
                .slug(id)
                .name(name)
                .tagline(tagline)
                .description(description)
                .price(BigDecimal.valueOf(price))
                .originalPrice(BigDecimal.valueOf(originalPrice))
                .durationWeeks(weeks)
                .hoursPerWeek(hoursPerWeek)
                .level(level)
                .leadMentorSlug(leadMentorSlug)
                .color(color)
                .iconKey(iconKey)
                .salary(salary)
                .medianSalary(medianSalary)
                .activeLearners(activeLearners)
                .enrolled(enrolled)
                .rating(rating)
                .reviews(reviews)
                .badge(badge)
                .nextCohort(nextCohort)
                .language(language)
                .certificate(certificate)
                .status("PUBLISHED")
                .skills(skills)
                .outcomes(outcomes)
                .build();

        int index = 0;
        for (Long courseId : courseIds) {
            TrackCourse tc = TrackCourse.builder()
                    .track(track)
                    .courseId(courseId)
                    .orderIndex(index++)
                    .build();
            track.getTrackCourses().add(tc);
        }
        return track;
    }

    private void seedCategories() {
        categoryRepository.saveAll(List.of(
                Category.builder().name("Cloud").slug("cloud").icon("Cloud").build(),
                Category.builder().name("AI / ML").slug("ai").icon("Sparkles").build(),
                Category.builder().name("Full Stack").slug("fullstack").icon("Code2").build(),
                Category.builder().name("DevOps").slug("devops").icon("Server").build(),
                Category.builder().name("Data Engineering").slug("data").icon("Database").build()
        ));
    }

    private void seedFaq() {
        faqRepository.saveAll(List.of(
                faq(1, "How is Cloud Nexus different from other LMS platforms?",
                        "We focus on career-aligned cloud, AI, devops, and full-stack tracks taught by ex-FAANG mentors."),
                faq(2, "Can I try Cloud Nexus before paying?",
                        "Yes. The Starter plan is free forever. Pro plan includes a 7-day free trial."),
                faq(3, "Are the certificates accredited?",
                        "Our certificates are industry-recognized and verifiable on LinkedIn via a unique credential URL.")
        ));
    }

    private Faq faq(int order, String question, String answer) {
        return Faq.builder().orderIndex(order).question(question).answer(answer).build();
    }

    private void seedTestimonials() {
        testimonialRepository.saveAll(List.of(
                Testimonial.builder().name("Aarav Sharma").role("Backend Engineer").company("@ Razorpay")
                        .course("AWS Solution Architect").rating(5)
                        .avatarUrl("https://randomuser.me/api/portraits/men/32.jpg")
                        .quote("The AWS track was the most structured cloud course I've taken.").build(),
                Testimonial.builder().name("Priya Verma").role("Frontend Developer").company("@ Swiggy")
                        .course("Modern JavaScript").rating(5)
                        .avatarUrl("https://randomuser.me/api/portraits/women/44.jpg")
                        .quote("Lessons were short, focused, and immediately applicable.").build()
        ));
    }

    private void seedHowItWorks() {
        howItWorksStepRepository.saveAll(List.of(
                step(1, "01", "Choose your path",
                        "Pick a curated career track or explore individual courses.", "Compass"),
                step(2, "02", "Learn with mentors",
                        "Watch lessons, join live mentor sessions, and ship hands-on projects.", "Users"),
                step(3, "03", "Earn a verified certificate",
                        "Pass the final assessment and receive an industry-recognized certificate.", "Award")
        ));
    }

    private HowItWorksStep step(int order, String step, String title, String description, String icon) {
        return HowItWorksStep.builder()
                .orderIndex(order).step(step).title(title).description(description).icon(icon)
                .build();
    }
}
