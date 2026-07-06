package com.lms.enrollment.service;

import java.util.Map;
import java.util.Optional;

public final class CatalogMetadata {

    public record CourseMeta(String title, String image, String badge, String instructor,
                             String rating, String duration, String modules, String description,
                             int totalLessons, Long courseId) {}

    private static final Map<String, CourseMeta> TRACK_COURSES = Map.ofEntries(
            Map.entry("cloud", new CourseMeta(
                    "AWS Solution Architect",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1600&auto=format&fit=crop",
                    "Intermediate", "Dr. Arjan Singh", "4.8", "24 Hours", "12 Mod",
                    "Master EC2, S3, and Lambda to build highly scalable and fault-tolerant cloud infrastructures.",
                    42, 1L)),
            Map.entry("ai", new CourseMeta(
                    "Azure Generative AI",
                    "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?q=80&w=1600&auto=format&fit=crop",
                    "Advanced", "Sarah Jenkins", "4.9", "18 Hours", "9 Mod",
                    "Dive deep into generative models, neural networks, and machine learning on Azure.",
                    32, 2L)),
            Map.entry("fullstack", new CourseMeta(
                    "Modern JavaScript",
                    "https://images.unsplash.com/photo-1498050108023-c5249f4df085?q=80&w=1600&auto=format&fit=crop",
                    "Beginner", "Prof. David Miller", "4.7", "30 Hours", "15 Mod",
                    "Build robust frontend applications with ES6+, asynchronous patterns, and scalable architectural designs.",
                    48, 3L)),
            Map.entry("devops", new CourseMeta(
                    "Docker & Containerization",
                    "https://images.unsplash.com/photo-1605745341112-85968b19335b?q=80&w=1600&auto=format&fit=crop",
                    "Intermediate", "James Wilson", "4.8", "20 Hours", "10 Mod",
                    "Containerize applications, orchestrate with Docker Compose, and ship production-ready images.",
                    36, 7L)),
            Map.entry("data", new CourseMeta(
                    "Python for Data Engineering",
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?q=80&w=1600&auto=format&fit=crop",
                    "Intermediate", "Angela Yu", "4.8", "28 Hours", "14 Mod",
                    "Build ETL pipelines, work with cloud warehouses, and orchestrate data workflows at scale.",
                    44, 5L)),
            Map.entry("backend", new CourseMeta(
                    "High Performance Go (Golang)",
                    "https://images.unsplash.com/photo-1555066931-4365d14bab8c?q=80&w=1600&auto=format&fit=crop",
                    "Advanced", "Elena Rodriguez", "4.9", "26 Hours", "13 Mod",
                    "Design APIs and services that scale to millions of requests with Go concurrency patterns.",
                    40, 4L))
    );

    private CatalogMetadata() {}

    public static Optional<CourseMeta> forTrack(String trackId) {
        return Optional.ofNullable(TRACK_COURSES.get(trackId));
    }

    public static int totalLessonsForTrack(String trackId) {
        return forTrack(trackId).map(CourseMeta::totalLessons).orElse(40);
    }

    public static Long courseIdForTrack(String trackId) {
        return forTrack(trackId).map(CourseMeta::courseId).orElse(null);
    }
}
