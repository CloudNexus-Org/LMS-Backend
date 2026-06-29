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
    String professor;
    String description;
    String image;
    Double rating;
    Integer reviews;
    String enrolled;
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

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .slug(course.getSlug())
                .title(course.getTitle())
                .professor(course.getProfessor())
                .description(course.getDescription())
                .image(course.getThumbnailUrl())
                .rating(course.getRating())
                .reviews(course.getReviewCount())
                .enrolled(course.getEnrolled())
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
                .build();
    }
}
