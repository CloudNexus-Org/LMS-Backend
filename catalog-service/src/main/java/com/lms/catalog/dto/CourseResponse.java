package com.lms.catalog.dto;

import com.lms.catalog.model.Course;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class CourseResponse {
    Long id;
    String slug;
    String title;
    /** Owning mentor user id — used for purchase / enrollment notifications. */
    Long mentorId;
    String professor;
    String description;
    String image;
    Double rating;
    Integer reviews;
    String enrolled;
    /** Raw enrollment / purchase count for analytics dashboards. */
    Integer enrollmentCount;
    String difficulty;
    String duration;
    Integer modules;
    Integer lessons;
    BigDecimal price;
    BigDecimal originalPrice;
    List<String> outcomes;
    List<String> skills;
    String exploreType;
    Boolean freePreview;
    String roadmap;
    String instructors;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .mentorId(course.getMentorId())
                .professor(course.getProfessor())
                .description(course.getDescription())
                .image(course.getThumbnailUrl())
                .rating(course.getRating())
                .reviews(course.getReviewCount())
                .enrolled(course.getEnrolled())
                .enrollmentCount(course.getEnrollmentCount() != null ? course.getEnrollmentCount() : 0)
                .difficulty(course.getDifficulty())
                .duration(course.getDuration())
                .modules(course.getModules())
                .lessons(course.getLessons())
                .price(course.getPrice())
                .originalPrice(course.getOriginalPrice())
                .outcomes(course.getOutcomes() == null ? List.of() : new ArrayList<>(course.getOutcomes()))
                .skills(course.getSkills() == null ? List.of() : new ArrayList<>(course.getSkills()))
                .exploreType(course.getExploreType())
                .freePreview(course.getFreePreview())
                .roadmap(course.getRoadmapJson())
                .instructors(course.getInstructorsJson())
                .build();
    }
}
