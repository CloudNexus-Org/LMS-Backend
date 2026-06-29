package com.lms.catalog.service;

import com.lms.catalog.dto.*;
import com.lms.catalog.event.CatalogEventProducer;
import com.lms.catalog.model.*;
import com.lms.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CatalogService {

    private static final String PUBLISHED = "PUBLISHED";
    private static final String PENDING = "PENDING";
    private static final String MENTOR_ROLE = "mentor";

    private final CourseRepository courseRepository;
    private final TrackRepository trackRepository;
    private final CategoryRepository categoryRepository;
    private final FaqRepository faqRepository;
    private final TestimonialRepository testimonialRepository;
    private final HowItWorksStepRepository howItWorksStepRepository;
    private final CatalogEventProducer eventProducer;

    @Value("${lms.admin-service-url}")
    private String adminServiceUrl;

    private final RestClient restClient = RestClient.create();

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
        courseRepository.findById(courseId).ifPresentOrElse(course -> {
            course.setStatus(PUBLISHED);
            course.setFeatured(true);
            courseRepository.save(course);
            log.info("Published course id={} slug={}", courseId, course.getSlug());
        }, () -> log.warn("Cannot publish — course not found id={}", courseId));
    }

    @Transactional
    public CourseResponse submitCourseForApproval(Long mentorId, String mentorRole, SubmitCourseRequest request) {
        requireMentor(mentorRole);
        validateSubmitRequest(request);

        String slug = uniqueSlug(slugify(request.getTitle()));
        BigDecimal price = request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO;
        BigDecimal originalPrice = price.compareTo(BigDecimal.ZERO) > 0
                ? price.multiply(BigDecimal.valueOf(2)).setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Course course = Course.builder()
                .slug(slug)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .mentorId(mentorId)
                .professor(request.getMentorName() != null ? request.getMentorName().trim() : "Mentor")
                .difficulty(request.getLevel())
                .duration(estimateDuration(request.getModules(), request.getLessons()))
                .modules(request.getModules())
                .lessons(request.getLessons())
                .price(price)
                .originalPrice(originalPrice)
                .rating(0.0)
                .reviewCount(0)
                .enrolled("0")
                .status(PENDING)
                .exploreType(mapCategoryToExploreType(request.getCategory()))
                .featured(false)
                .freePreview(true)
                .outcomes(request.getOutcomes() != null
                        ? request.getOutcomes().stream().filter(o -> o != null && !o.isBlank()).toList()
                        : List.of())
                .skills(request.getTags() != null ? request.getTags() : List.of())
                .build();

        course = courseRepository.save(course);
        String courseCode = "C-" + course.getId();

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("courseId", course.getId());
        event.put("courseCode", courseCode);
        event.put("title", course.getTitle());
        event.put("description", course.getDescription());
        event.put("mentorId", mentorId);
        event.put("mentorName", course.getProfessor());
        event.put("mentorAvatar", initials(course.getProfessor()));
        event.put("category", request.getCategory() != null ? request.getCategory() : "General");
        event.put("modules", course.getModules() != null ? course.getModules() : 0);
        event.put("lessons", course.getLessons() != null ? course.getLessons() : 0);
        event.put("duration", course.getDuration());
        event.put("thumbnail", pickThumbnail(course.getId()));
        event.put("priority", (course.getLessons() != null && course.getLessons() >= 20) ? "high" : "normal");
        eventProducer.publishCourseSubmitted(event);
        syncApprovalToAdmin(event);

        return CourseResponse.from(course);
    }

    private void syncApprovalToAdmin(Map<String, Object> event) {
        try {
            restClient.post()
                    .uri(adminServiceUrl + "/api/admin/internal/approvals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Admin sync failed (Kafka consumer may still process): {}", ex.getMessage());
        }
    }

    private void requireMentor(String role) {
        if (role == null || !MENTOR_ROLE.equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor role required");
        }
    }

    private void validateSubmitRequest(SubmitCourseRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course title must be at least 10 characters");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().length() < 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be at least 40 characters");
        }
        if (request.getModules() == null || request.getModules() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one module is required");
        }
        if (request.getLessons() == null || request.getLessons() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one lesson is required");
        }
    }

    private String slugify(String title) {
        String slug = title.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (slug.length() > 80) {
            slug = slug.substring(0, 80).replaceAll("-+$", "");
        }
        return slug.isBlank() ? "course" : slug;
    }

    private String uniqueSlug(String base) {
        if (courseRepository.findBySlug(base).isEmpty()) {
            return base;
        }
        return base + "-" + System.currentTimeMillis() % 100000;
    }

    private String estimateDuration(Integer modules, Integer lessons) {
        int mins = (lessons != null ? lessons : 1) * 15;
        int hours = mins / 60;
        int rem = mins % 60;
        if (hours > 0) {
            return hours + "h " + (rem > 0 ? rem + "m" : "00m");
        }
        return rem + "m";
    }

    private String mapCategoryToExploreType(String category) {
        if (category == null) return "fullstack";
        return switch (category.toLowerCase()) {
            case "cloud & devops" -> "devops";
            case "data & ai" -> "ai";
            case "backend systems" -> "cloud";
            case "system design" -> "cloud";
            case "mobile dev" -> "fullstack";
            default -> "fullstack";
        };
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "MN";
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .limit(2)
                .map(s -> String.valueOf(s.charAt(0)).toUpperCase())
                .collect(Collectors.joining());
    }

    private String pickThumbnail(Long courseId) {
        List<String> gradients = List.of(
                "from-blue-500 to-cyan-400",
                "from-emerald-500 to-teal-400",
                "from-violet-500 to-fuchsia-400",
                "from-orange-500 to-red-400"
        );
        return gradients.get((int) (courseId % gradients.size()));
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
