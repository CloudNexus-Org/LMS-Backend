package com.lms.certificate.service;

import com.lms.certificate.client.CatalogClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CertificateMetadataResolver {

    private final CatalogClient catalogClient;

    public TrackMetadata.Meta resolve(String trackId, Long courseIdHint) {
        var staticMeta = TrackMetadata.forTrack(trackId);
        if (staticMeta.isPresent()) {
            return staticMeta.get();
        }

        Long courseId = courseIdHint;
        if (courseId == null && trackId != null && trackId.startsWith("course-")) {
            try {
                courseId = Long.parseLong(trackId.substring("course-".length()));
            } catch (NumberFormatException ignored) {
                courseId = null;
            }
        }

        final Long resolvedCourseId = courseId;
        if (resolvedCourseId != null) {
            return catalogClient.findCourse(resolvedCourseId)
                    .map(this::fromCatalog)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Course not found for certificate: " + resolvedCourseId));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown track: " + trackId);
    }

    private TrackMetadata.Meta fromCatalog(CatalogClient.CourseSnapshot course) {
        String trackLabel = formatTrackLabel(course.badge(), course.exploreType());
        String description = course.description();
        if (description == null || description.isBlank()) {
            description = "Successfully completed all lessons and assessments in this course.";
        }
        String duration = course.duration();
        if (duration == null || duration.isBlank()) {
            duration = "—";
        }
        String mentor = course.instructor();
        if (mentor == null || mentor.isBlank()) {
            mentor = "Realm Mentor";
        }
        return new TrackMetadata.Meta(
                course.title(),
                description,
                duration,
                trackLabel,
                mentor,
                "CN-C" + course.courseId()
        );
    }

    private static String formatTrackLabel(String badge, String exploreType) {
        if (exploreType != null && !exploreType.isBlank()) {
            return switch (exploreType.toLowerCase()) {
                case "cloud" -> "Cloud Engineering";
                case "ai", "ml" -> "AI / ML Engineering";
                case "devops" -> "DevOps Engineering";
                case "fullstack", "dev" -> "Full-Stack Development";
                case "data" -> "Data Engineering";
                default -> capitalize(exploreType) + " Track";
            };
        }
        if (badge != null && !badge.isBlank()) {
            return badge + " Track";
        }
        return "Professional Development";
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
