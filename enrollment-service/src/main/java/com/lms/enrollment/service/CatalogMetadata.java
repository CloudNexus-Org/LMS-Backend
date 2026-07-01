package com.lms.enrollment.service;

import java.util.Map;
import java.util.Optional;

public final class CatalogMetadata {

    public record CourseMeta(String title, String image, String badge, String instructor,
                             String rating, String duration, String modules, String description,
                             int totalLessons, Long courseId) {}

    private static final Map<String, CourseMeta> TRACK_COURSES = Map.of(
            "cloud", new CourseMeta(
                    "AWS Solution Architect",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1600&auto=format&fit=crop",
                    "Intermediate", "Dr. Arjan Singh", "4.8", "24 Hours", "12 Mod",
                    "Master EC2, S3, and Lambda to build highly scalable and fault-tolerant cloud infrastructures.",
                    42, 1L),
            "ai", new CourseMeta(
                    "Azure Generative AI",
                    "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?q=80&w=1600&auto=format&fit=crop",
                    "Advanced", "Sarah Jenkins", "4.9", "18 Hours", "9 Mod",
                    "Dive deep into generative models, neural networks, and machine learning on Azure.",
                    32, 2L)
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
