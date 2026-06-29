package com.lms.catalog.dto;

import com.lms.catalog.model.Track;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class TrackResponse {
    String id;
    String slug;
    String name;
    String tagline;
    String longDescription;
    List<Long> courseIds;
    String color;
    String iconKey;
    String leadMentorSlug;
    String salary;
    String medianSalary;
    Integer activeLearners;
    String enrolled;
    Double rating;
    Integer reviews;
    String badge;
    Integer durationWeeks;
    String hoursPerWeek;
    String nextCohort;
    String level;
    String language;
    String certificate;
    BigDecimal price;
    BigDecimal originalPrice;
    List<String> skills;
    List<String> outcomes;

    public static TrackResponse from(Track track) {
        List<Long> courseIds = track.getTrackCourses().stream()
                .map(tc -> tc.getCourseId())
                .toList();
        return TrackResponse.builder()
                .id(track.getId())
                .slug(track.getSlug())
                .name(track.getName())
                .tagline(track.getTagline())
                .longDescription(track.getDescription())
                .courseIds(courseIds)
                .color(track.getColor())
                .iconKey(track.getIconKey())
                .leadMentorSlug(track.getLeadMentorSlug())
                .salary(track.getSalary())
                .medianSalary(track.getMedianSalary())
                .activeLearners(track.getActiveLearners())
                .enrolled(track.getEnrolled())
                .rating(track.getRating())
                .reviews(track.getReviews())
                .badge(track.getBadge())
                .durationWeeks(track.getDurationWeeks())
                .hoursPerWeek(track.getHoursPerWeek())
                .nextCohort(track.getNextCohort())
                .level(track.getLevel())
                .language(track.getLanguage())
                .certificate(track.getCertificate())
                .price(track.getPrice())
                .originalPrice(track.getOriginalPrice())
                .skills(track.getSkills() == null ? List.of() : new ArrayList<>(track.getSkills()))
                .outcomes(track.getOutcomes() == null ? List.of() : new ArrayList<>(track.getOutcomes()))
                .build();
    }
}
