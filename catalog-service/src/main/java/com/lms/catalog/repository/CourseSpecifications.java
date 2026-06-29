package com.lms.catalog.repository;

import com.lms.catalog.model.Course;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> published() {
        return (root, query, cb) -> cb.equal(root.get("status"), "PUBLISHED");
    }

    public static Specification<Course> withDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("difficulty")), difficulty.toLowerCase());
    }

    public static Specification<Course> withExploreType(String exploreType) {
        if (exploreType == null || exploreType.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("exploreType")), exploreType.toLowerCase());
    }

    public static Specification<Course> withMinPrice(BigDecimal minPrice) {
        if (minPrice == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Course> withMaxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Course> matchesSearch(String q) {
        if (q == null || q.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Course> search(
            String q, String difficulty, String exploreType,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return Specification.where(published())
                .and(withDifficulty(difficulty))
                .and(withExploreType(exploreType))
                .and(withMinPrice(minPrice))
                .and(withMaxPrice(maxPrice))
                .and(matchesSearch(q));
    }
}
