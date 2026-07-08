package com.lms.enrollment.service;

import java.util.Optional;

/** Lightweight defaults when catalog is unreachable — no demo course data. */
public final class CatalogMetadata {

    public record CourseMeta(String title, String image, String badge, String instructor,
                             String rating, String duration, String modules, String description,
                             int totalLessons, Long courseId) {}

    private CatalogMetadata() {}

    public static Optional<CourseMeta> forTrack(String trackId) {
        return Optional.empty();
    }

    public static Optional<CourseMeta> forCourseId(Long courseId) {
        return Optional.empty();
    }

    public static Optional<String> trackIdForCourse(Long courseId) {
        return Optional.empty();
    }

    public static int totalLessonsForCourse(Long courseId) {
        return 0;
    }

    public static int totalLessonsForTrack(String trackId) {
        return 0;
    }

    public static Long courseIdForTrack(String trackId) {
        return null;
    }
}
