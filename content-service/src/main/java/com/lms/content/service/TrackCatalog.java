package com.lms.content.service;

import java.util.Map;

/** Maps career track slugs → primary catalog course IDs (matches LMS frontend tracks.js). */
public final class TrackCatalog {

    private static final Map<String, Long> PRIMARY_COURSE = Map.of(
            "cloud", 1L,
            "ai", 2L,
            "fullstack", 3L,
            "backend", 4L,
            "data", 5L,
            "devops", 7L
    );

    private TrackCatalog() {}

    public static Long primaryCourseId(String trackId) {
        return PRIMARY_COURSE.get(trackId);
    }
}
