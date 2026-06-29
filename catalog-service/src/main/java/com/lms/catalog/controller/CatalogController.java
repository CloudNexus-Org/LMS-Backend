package com.lms.catalog.controller;

import com.lms.catalog.dto.CourseResponse;
import com.lms.catalog.dto.FilterOptionsResponse;
import com.lms.catalog.dto.PagedResponse;
import com.lms.catalog.dto.TestimonialResponse;
import com.lms.catalog.dto.TrackResponse;
import com.lms.catalog.model.Category;
import com.lms.catalog.model.Faq;
import com.lms.catalog.model.HowItWorksStep;
import com.lms.catalog.model.Testimonial;
import com.lms.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/courses")
    public PagedResponse<CourseResponse> listCourses(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String exploreType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.listCourses(difficulty, exploreType, minPrice, maxPrice, sort, page, size);
    }

    @GetMapping("/courses/featured")
    public List<CourseResponse> featuredCourses() {
        return catalogService.getFeaturedCourses();
    }

    @GetMapping("/courses/filters")
    public FilterOptionsResponse filters() {
        return catalogService.getFilters();
    }

    @GetMapping("/courses/{slug}")
    public CourseResponse courseBySlug(@PathVariable String slug) {
        return catalogService.getCourseBySlug(slug);
    }

    @GetMapping("/courses/{slug}/preview")
    public Map<String, Object> coursePreview(@PathVariable String slug) {
        return catalogService.getCoursePreview(slug);
    }

    @GetMapping("/tracks")
    public List<TrackResponse> tracks() {
        return catalogService.listTracks();
    }

    @GetMapping("/tracks/{id}")
    public TrackResponse trackDetail(@PathVariable String id) {
        return catalogService.getTrack(id);
    }

    @GetMapping("/tracks/{id}/courses")
    public List<CourseResponse> trackCourses(@PathVariable String id) {
        return catalogService.getTrackCourses(id);
    }

    @GetMapping("/explore/{type}")
    public List<CourseResponse> explore(@PathVariable String type) {
        return catalogService.exploreByType(type);
    }

    @GetMapping("/explore/search")
    public PagedResponse<CourseResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.search(q, difficulty, page, size);
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/stats/public")
    public Map<String, Object> publicStats() {
        return catalogService.getPublicStats();
    }

    @GetMapping("/faq")
    public List<Faq> faq() {
        return catalogService.listFaq();
    }

    @GetMapping("/testimonials")
    public List<TestimonialResponse> testimonials() {
        return catalogService.listTestimonials().stream()
                .map(TestimonialResponse::from)
                .toList();
    }

    @GetMapping("/how-it-works")
    public List<HowItWorksStep> howItWorks() {
        return catalogService.listHowItWorks();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "catalog-service");
    }
}
