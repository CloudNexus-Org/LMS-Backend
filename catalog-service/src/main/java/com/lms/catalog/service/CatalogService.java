package com.lms.catalog.service;

import com.lms.catalog.dto.*;
import com.lms.catalog.model.*;
import com.lms.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private static final String PUBLISHED = "PUBLISHED";

    private final CourseRepository courseRepository;
    private final TrackRepository trackRepository;
    private final CategoryRepository categoryRepository;
    private final FaqRepository faqRepository;
    private final TestimonialRepository testimonialRepository;
    private final HowItWorksStepRepository howItWorksStepRepository;

    public PagedResponse<CourseResponse> listCourses(
            String difficulty, String exploreType, BigDecimal minPrice, BigDecimal maxPrice,
            String sort, int page, int size) {

        Sort sortSpec = resolveSort(sort);
        Page<Course> result = courseRepository.findAll(
                CourseSpecifications.search(null, difficulty, exploreType, minPrice, maxPrice),
                PageRequest.of(page, size, sortSpec));

        return toPaged(result);
    }

    public CourseResponse getCourseBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .filter(c -> PUBLISHED.equals(c.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return CourseResponse.from(course);
    }

    public List<CourseResponse> getFeaturedCourses() {
        return courseRepository.findByFeaturedTrueAndStatus(PUBLISHED).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<TrackResponse> listTracks() {
        return trackRepository.findAll().stream()
                .filter(t -> PUBLISHED.equals(t.getStatus()))
                .map(TrackResponse::from)
                .toList();
    }

    public TrackResponse getTrack(String id) {
        Track track = trackRepository.findById(id)
                .or(() -> trackRepository.findBySlug(id))
                .filter(t -> PUBLISHED.equals(t.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found"));
        return TrackResponse.from(track);
    }

    public List<CourseResponse> getTrackCourses(String id) {
        Track track = trackRepository.findById(id)
                .or(() -> trackRepository.findBySlug(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found"));

        List<Long> courseIds = track.getTrackCourses().stream()
                .map(TrackCourse::getCourseId)
                .toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return courseRepository.findByIdIn(courseIds).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public List<CourseResponse> exploreByType(String type) {
        return courseRepository.findByExploreTypeIgnoreCaseAndStatus(type, PUBLISHED).stream()
                .map(CourseResponse::from)
                .toList();
    }

    public PagedResponse<CourseResponse> search(String q, String difficulty, int page, int size) {
        Page<Course> result = courseRepository.findAll(
                CourseSpecifications.search(q, difficulty, null, null, null),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating")));
        return toPaged(result);
    }

    public List<Category> listCategories() {
        return categoryRepository.findAll();
    }

    public Map<String, Object> getCoursePreview(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return Map.of(
                "slug", course.getSlug(),
                "title", course.getTitle(),
                "freePreview", Boolean.TRUE.equals(course.getFreePreview()),
                "previewLessonTitle", "Introduction — " + course.getTitle(),
                "previewDuration", "12 min"
        );
    }

    public FilterOptionsResponse getFilters() {
        List<Course> published = courseRepository.findByStatus(PUBLISHED);
        List<String> difficulties = published.stream()
                .map(Course::getDifficulty)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        List<String> exploreTypes = published.stream()
                .map(Course::getExploreType)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        BigDecimal min = published.stream().map(Course::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = published.stream().map(Course::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return FilterOptionsResponse.builder()
                .difficulties(difficulties)
                .exploreTypes(exploreTypes)
                .priceRange(Map.of("min", min, "max", max))
                .build();
    }

    public Map<String, Object> getPublicStats() {
        long courseCount = courseRepository.findByStatus(PUBLISHED).size();
        long trackCount = trackRepository.findAll().stream()
                .filter(t -> PUBLISHED.equals(t.getStatus()))
                .count();
        return Map.of(
                "totalCourses", courseCount,
                "totalTracks", trackCount,
                "totalLearners", "50k+",
                "totalMentors", 24
        );
    }

    public List<Faq> listFaq() {
        return faqRepository.findAllByOrderByOrderIndexAsc();
    }

    public List<Testimonial> listTestimonials() {
        return testimonialRepository.findAll();
    }

    public List<HowItWorksStep> listHowItWorks() {
        return howItWorksStepRepository.findAllByOrderByOrderIndexAsc();
    }

    @Transactional
    public void updateCourseRating(Long courseId, Double avgRating, Integer reviewCount) {
        courseRepository.findById(courseId).ifPresent(course -> {
            course.setRating(avgRating);
            course.setReviewCount(reviewCount);
            courseRepository.save(course);
        });
    }

    @Transactional
    public void publishCourse(Long courseId) {
        courseRepository.findById(courseId).ifPresent(course -> {
            course.setStatus(PUBLISHED);
            courseRepository.save(course);
        });
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }

    private PagedResponse<CourseResponse> toPaged(Page<Course> page) {
        return PagedResponse.<CourseResponse>builder()
                .content(page.getContent().stream().map(CourseResponse::from).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
