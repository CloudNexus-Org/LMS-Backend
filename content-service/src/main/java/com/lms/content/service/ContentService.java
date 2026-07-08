package com.lms.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.content.client.AdminClient;
import com.lms.content.client.CatalogClient;
import com.lms.content.client.MentorClient;
import com.lms.content.dto.*;
import com.lms.content.event.ContentEventPublisher;
import com.lms.content.util.CourseSubmissionRules;
import com.lms.content.model.*;
import com.lms.content.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final CourseContentRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository resourceRepository;
    private final LessonTranscriptRepository transcriptRepository;
    private final ContentEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final MentorClient mentorClient;
    private final CatalogClient catalogClient;
    private final AdminClient adminClient;

    @Transactional
    public CourseResponse createCourse(Long mentorId, String role, CreateCourseRequest request) {
        mentorOnly(mentorId, role);
        CourseContent course = CourseContent.builder()
                .mentorId(mentorId)
                .status(CourseStatus.DRAFT)
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel())
                .language(request.getLanguage() != null ? request.getLanguage() : "English")
                .outcomesJson(toJson(request.getOutcomes()))
                .tagsJson(toJson(request.getTags()))
                .requirements(request.getRequirements())
                .trackId(request.getTrackId())
                .thumbnailUrl(request.getThumbnailUrl())
                .pricingPlan("free")
                .build();
        return toCourseResponse(courseRepository.save(course), true);
    }

    @Transactional
    public CourseResponse updateCourse(Long mentorId, String role, Long courseId, UpdateCourseRequest request) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getSubtitle() != null) course.setSubtitle(request.getSubtitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getLanguage() != null) course.setLanguage(request.getLanguage());
        if (request.getOutcomes() != null) course.setOutcomesJson(toJson(request.getOutcomes()));
        if (request.getTags() != null) course.setTagsJson(toJson(request.getTags()));
        if (request.getRequirements() != null) course.setRequirements(request.getRequirements());
        if (request.getTrackId() != null) course.setTrackId(request.getTrackId());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        return toCourseResponse(courseRepository.save(course), true);
    }

    public CourseResponse getCourse(Long mentorId, String role, Long courseId) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        return toCourseResponse(course, true);
    }

    public List<CourseResponse> listDrafts(Long mentorId, String role) {
        mentorOnly(mentorId, role);
        return courseRepository.findByMentorIdOrderByUpdatedAtDesc(mentorId).stream()
                .map(c -> toCourseResponse(c, false))
                .toList();
    }

    @Transactional
    public ModuleResponse addModule(Long mentorId, String role, Long courseId, ModuleRequest request) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        int order = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).size();
        CourseModule module = CourseModule.builder()
                .courseId(courseId)
                .title(request.getTitle() != null ? request.getTitle() : "New Module")
                .description(request.getDescription())
                .orderIndex(order)
                .build();
        return toModuleResponse(moduleRepository.save(module), true);
    }

    @Transactional
    public ModuleResponse updateModule(Long mentorId, String role, Long courseId, Long moduleId, ModuleRequest request) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        CourseModule module = getModuleInCourse(courseId, moduleId);
        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());
        return toModuleResponse(moduleRepository.save(module), true);
    }

    @Transactional
    public Map<String, String> deleteModule(Long mentorId, String role, Long courseId, Long moduleId) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        CourseModule module = getModuleInCourse(courseId, moduleId);
        lessonRepository.deleteByModuleId(moduleId);
        moduleRepository.delete(module);
        return Map.of("message", "Module deleted");
    }

    @Transactional
    public LessonResponse addLesson(Long mentorId, String role, Long courseId, Long moduleId, LessonRequest request) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        getModuleInCourse(courseId, moduleId);
        int order = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : lessonRepository.findByModuleIdOrderByOrderIndexAsc(moduleId).size();
        Lesson lesson = Lesson.builder()
                .moduleId(moduleId)
                .title(request.getTitle() != null ? request.getTitle() : "New Lesson")
                .type(request.getType() != null ? request.getType() : "video")
                .durationMin(request.getDurationMin())
                .orderIndex(order)
                .contentUrl(request.getContentUrl())
                .readingContent(request.getReadingContent())
                .previewFree(Boolean.TRUE.equals(request.getPreviewFree()))
                .summary(request.getSummary())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        eventPublisher.publishLessonCreated(saved, courseId, mentorId);
        return toLessonResponse(saved, courseId, null);
    }

    @Transactional
    public LessonResponse updateLesson(Long mentorId, String role, Long courseId, Long lessonId, LessonRequest request) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        Lesson lesson = getLessonInCourse(courseId, lessonId);
        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getType() != null) lesson.setType(request.getType());
        if (request.getDurationMin() != null) lesson.setDurationMin(request.getDurationMin());
        if (request.getOrderIndex() != null) lesson.setOrderIndex(request.getOrderIndex());
        if (request.getContentUrl() != null) lesson.setContentUrl(request.getContentUrl());
        if (request.getReadingContent() != null) lesson.setReadingContent(request.getReadingContent());
        if (request.getPreviewFree() != null) lesson.setPreviewFree(request.getPreviewFree());
        if (request.getSummary() != null) lesson.setSummary(request.getSummary());
        return toLessonResponse(lessonRepository.save(lesson), courseId, null);
    }

    @Transactional
    public Map<String, String> deleteLesson(Long mentorId, String role, Long courseId, Long lessonId) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        Lesson lesson = getLessonInCourse(courseId, lessonId);
        lessonRepository.delete(lesson);
        return Map.of("message", "Lesson deleted");
    }

    @Transactional
    public CourseResponse reorderCurriculum(Long mentorId, String role, Long courseId, ReorderRequest request) {
        mentorOnly(mentorId, role);
        getOwnedCourse(mentorId, courseId);
        if (request.getModules() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modules required");
        }
        for (ReorderRequest.ModuleOrder mo : request.getModules()) {
            CourseModule module = getModuleInCourse(courseId, mo.getModuleId());
            if (mo.getOrderIndex() != null) module.setOrderIndex(mo.getOrderIndex());
            moduleRepository.save(module);
            if (mo.getLessons() != null) {
                for (ReorderRequest.LessonOrder lo : mo.getLessons()) {
                    Lesson lesson = getLessonInModule(module.getId(), lo.getLessonId());
                    if (lo.getOrderIndex() != null) lesson.setOrderIndex(lo.getOrderIndex());
                    lessonRepository.save(lesson);
                }
            }
        }
        return toCourseResponse(courseRepository.findById(courseId).orElseThrow(), true);
    }

    @Transactional
    public CourseResponse submitForApproval(Long mentorId, String role, Long courseId) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course title is required");
        }
        course.setStatus(CourseStatus.PENDING);
        course.setSubmittedAt(Instant.now());
        CourseContent saved = courseRepository.save(course);
        notifySubmissionPipeline(saved);
        return toCourseResponse(saved, true);
    }

    @Transactional
    public void backfillPendingApprovals() {
        for (CourseContent course : courseRepository.findByStatus(CourseStatus.PENDING)) {
            if (!isEligibleForApprovalQueue(course)) {
                continue;
            }
            try {
                notifySubmissionPipeline(course);
            } catch (Exception ex) {
                // continue with other pending courses
            }
        }
    }

    @Transactional
    public void purgeIneligibleSubmissions() {
        List<CourseContent> pending = courseRepository.findByStatus(CourseStatus.PENDING);
        for (CourseContent course : pending) {
            if (isEligibleForApprovalQueue(course)) {
                continue;
            }
            purgeCourseCurriculum(course.getId());
            courseRepository.delete(course);
        }
    }

    private boolean isEligibleForApprovalQueue(CourseContent course) {
        if (CourseSubmissionRules.isAutomatedTest(course)) {
            return false;
        }
        return mentorClient.isActiveMentor(course.getMentorId());
    }

    private void notifySubmissionPipeline(CourseContent course) {
        if (!isEligibleForApprovalQueue(course)) {
            return;
        }
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
        int moduleCount = modules.size();
        int lessonCount = modules.stream()
                .mapToInt(m -> lessonRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).size())
                .sum();

        String mentorName = mentorClient.resolveMentorName(course.getMentorId()).orElse("Mentor");
        BigDecimal price = course.getPrice() != null
                ? BigDecimal.valueOf(course.getPrice())
                : BigDecimal.ZERO;

        Long catalogCourseId = catalogClient.syncPendingFromContent(new CatalogClient.SyncPayload(
                course.getId(),
                course.getCourseId(),
                course.getMentorId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getLevel(),
                moduleCount,
                lessonCount,
                price,
                course.getThumbnailUrl(),
                mentorName,
                fromJsonList(course.getOutcomesJson()),
                fromJsonList(course.getTagsJson())
        )).orElse(course.getCourseId());

        if (catalogCourseId != null && !Objects.equals(course.getCourseId(), catalogCourseId)) {
            course.setCourseId(catalogCourseId);
            courseRepository.save(course);
        }

        Map<String, Object> event = buildSubmissionEvent(course, catalogCourseId, mentorName, moduleCount, lessonCount);
        eventPublisher.publishCourseSubmitted(event);
        adminClient.syncCourseSubmission(event);
    }

    private Map<String, Object> buildSubmissionEvent(CourseContent course, Long catalogCourseId,
                                                     String mentorName, int moduleCount, int lessonCount) {
        Long catalogId = catalogCourseId != null ? catalogCourseId : course.getId();
        String courseCode = "C-" + catalogId;
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("contentId", course.getId());
        event.put("courseId", catalogId);
        event.put("courseCode", courseCode);
        event.put("title", course.getTitle());
        event.put("description", course.getDescription());
        event.put("mentorId", course.getMentorId());
        event.put("mentorName", mentorName);
        event.put("mentorAvatar", initials(mentorName));
        event.put("category", course.getCategory() != null ? course.getCategory() : "General");
        event.put("modules", moduleCount);
        event.put("lessons", lessonCount);
        event.put("duration", estimateDuration(moduleCount, lessonCount));
        event.put("thumbnail", pickThumbnail(catalogId));
        event.put("priority", lessonCount >= 20 ? "high" : "normal");
        return event;
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "MN";
        }
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .limit(2)
                .map(s -> String.valueOf(s.charAt(0)).toUpperCase())
                .collect(Collectors.joining());
    }

    private static String estimateDuration(int modules, int lessons) {
        int mins = Math.max(lessons, 1) * 15;
        int hours = mins / 60;
        int rem = mins % 60;
        if (hours > 0) {
            return hours + "h " + (rem > 0 ? rem + "m" : "00m");
        }
        return rem + "m";
    }

    private static String pickThumbnail(Long courseId) {
        List<String> gradients = List.of(
                "from-blue-500 to-cyan-400",
                "from-emerald-500 to-teal-400",
                "from-violet-500 to-fuchsia-400",
                "from-orange-500 to-red-400"
        );
        return gradients.get((int) (courseId % gradients.size()));
    }

    @Transactional
    public CourseResponse updatePricing(Long mentorId, String role, Long courseId, PricingRequest request) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (request.getPricingPlan() != null) course.setPricingPlan(request.getPricingPlan());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        return toCourseResponse(courseRepository.save(course), true);
    }

    @Transactional
    public CourseResponse publishCourse(Long mentorId, String role, Long courseId) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (course.getStatus() != CourseStatus.APPROVED && course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Course must be approved before publishing");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        if (course.getCourseId() == null) course.setCourseId(course.getId());
        CourseContent saved = courseRepository.save(course);
        eventPublisher.publishCoursePublished(saved);
        return toCourseResponse(saved, true);
    }

    @Transactional
    public Map<String, String> deleteCourse(Long mentorId, String role, Long courseId) {
        mentorOnly(mentorId, role);
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (course.getStatus() == CourseStatus.PUBLISHED || course.getStatus() == CourseStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Published or approved courses cannot be deleted");
        }
        purgeCourseCurriculum(courseId);
        courseRepository.delete(course);
        return Map.of("message", "Course deleted", "courseId", String.valueOf(courseId));
    }

    public List<LessonResponse> getTrackLessons(String trackId) {
        List<CourseContent> courses = resolveCoursesForTrack(trackId);
        if (courses.isEmpty()) {
            return List.of();
        }
        Long primaryCatalogId = TrackCatalog.primaryCourseId(trackId);
        if (primaryCatalogId != null) {
            List<CourseContent> primary = courses.stream()
                    .filter(c -> primaryCatalogId.equals(c.getCourseId()))
                    .toList();
            if (!primary.isEmpty()) {
                courses = primary;
            }
        }
        return buildOrderedLessonList(courses);
    }

    public List<LessonResponse> getCatalogCourseLessons(Long catalogCourseId) {
        List<CourseContent> courses = courseRepository.findByCourseId(catalogCourseId).stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED
                        || c.getStatus() == CourseStatus.APPROVED)
                .toList();
        return buildOrderedLessonList(courses);
    }

    private List<CourseContent> resolveCoursesForTrack(String trackId) {
        if (trackId != null && trackId.startsWith("course-")) {
            String rawId = trackId.substring("course-".length());
            try {
                Long catalogCourseId = Long.parseLong(rawId);
                return courseRepository.findByCourseId(catalogCourseId).stream()
                        .filter(c -> c.getStatus() == CourseStatus.PUBLISHED
                                || c.getStatus() == CourseStatus.APPROVED)
                        .toList();
            } catch (NumberFormatException ignored) {
                return List.of();
            }
        }
        return courseRepository.findByTrackId(trackId).stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED
                        || c.getStatus() == CourseStatus.APPROVED)
                .toList();
    }

    private List<LessonResponse> buildOrderedLessonList(List<CourseContent> courses) {
        List<LessonResponse> result = new ArrayList<>();
        int order = 1;
        for (CourseContent course : courses) {
            List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
            for (CourseModule module : modules) {
                List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
                for (Lesson lesson : lessons) {
                    LessonResponse lr = toLessonResponse(lesson, course.getId(), course.getTitle());
                    lr.setOrderIndex(order++);
                    result.add(lr);
                }
            }
        }
        return result;
    }

    public LessonResponse getLessonDetail(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        Long courseId = findCourseIdForLesson(lesson);
        String courseTitle = courseRepository.findById(courseId).map(CourseContent::getTitle).orElse(null);
        return toLessonResponse(lesson, courseId, courseTitle);
    }

    public List<ResourceResponse> getLessonResources(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
        }
        return resourceRepository.findByLessonIdOrderByIdAsc(lessonId).stream()
                .map(this::toResourceResponse)
                .toList();
    }

    public TranscriptResponse getLessonTranscript(Long lessonId) {
        LessonTranscript transcript = transcriptRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transcript not found"));
        return TranscriptResponse.builder()
                .lessonId(lessonId)
                .language(transcript.getLanguage())
                .transcriptText(transcript.getTranscriptText())
                .lines(parseTranscriptLines(transcript.getTranscriptText()))
                .build();
    }

    @Transactional
    public void handleCourseApproved(Map<String, Object> event) {
        Long catalogId = longVal(event.get("courseId"));
        if (catalogId != null) {
            List<CourseContent> linked = courseRepository.findByCourseId(catalogId);
            if (!linked.isEmpty()) {
                linked.forEach(course -> {
                    course.setStatus(CourseStatus.APPROVED);
                    courseRepository.save(course);
                });
                return;
            }
        }
        Long contentId = longVal(event.get("contentId"));
        if (contentId == null) {
            contentId = catalogId;
        }
        if (contentId == null) {
            return;
        }
        courseRepository.findById(contentId).ifPresent(course -> {
            course.setStatus(CourseStatus.APPROVED);
            courseRepository.save(course);
        });
    }

    @Transactional
    public void handleCourseRejected(Map<String, Object> event) {
        Long catalogId = longVal(event.get("courseId"));
        if (catalogId != null) {
            List<CourseContent> linked = courseRepository.findByCourseId(catalogId);
            if (!linked.isEmpty()) {
                linked.forEach(course -> {
                    course.setStatus(CourseStatus.REJECTED);
                    courseRepository.save(course);
                });
                return;
            }
        }
        Long contentId = longVal(event.get("contentId"));
        if (contentId == null) {
            contentId = catalogId;
        }
        if (contentId == null) {
            return;
        }
        courseRepository.findById(contentId).ifPresent(course -> {
            course.setStatus(CourseStatus.REJECTED);
            courseRepository.save(course);
        });
    }

    private CourseContent getOwnedCourse(Long mentorId, Long courseId) {
        CourseContent course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!Objects.equals(course.getMentorId(), mentorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course");
        }
        return course;
    }

    private CourseModule getModuleInCourse(Long courseId, Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!Objects.equals(module.getCourseId(), courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module not in course");
        }
        return module;
    }

    private Lesson getLessonInCourse(Long courseId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        getModuleInCourse(courseId, lesson.getModuleId());
        return lesson;
    }

    private Lesson getLessonInModule(Long moduleId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!Objects.equals(lesson.getModuleId(), moduleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson not in module");
        }
        return lesson;
    }

    private Long findCourseIdForLesson(Lesson lesson) {
        return moduleRepository.findById(lesson.getModuleId())
                .map(CourseModule::getCourseId)
                .orElse(null);
    }

    private void purgeCourseCurriculum(Long courseId) {
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        for (CourseModule module : modules) {
            List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
            for (Lesson lesson : lessons) {
                resourceRepository.deleteAll(
                        resourceRepository.findByLessonIdOrderByIdAsc(lesson.getId()));
                if (transcriptRepository.existsById(lesson.getId())) {
                    transcriptRepository.deleteById(lesson.getId());
                }
            }
            lessonRepository.deleteByModuleId(module.getId());
        }
        moduleRepository.deleteByCourseId(courseId);
    }

    private void mentorOnly(Long mentorId, String role) {
        if (mentorId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id required");
        }
        if (role == null || !"MENTOR".equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor role required");
        }
    }

    private CourseResponse toCourseResponse(CourseContent course, boolean includeModules) {
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
        int lessonCount = modules.stream()
                .mapToInt(m -> lessonRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).size())
                .sum();
        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .courseId(course.getCourseId())
                .mentorId(course.getMentorId())
                .status(course.getStatus().name())
                .pricingPlan(course.getPricingPlan())
                .price(course.getPrice())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .level(course.getLevel())
                .language(course.getLanguage())
                .outcomes(fromJsonList(course.getOutcomesJson()))
                .tags(fromJsonList(course.getTagsJson()))
                .requirements(course.getRequirements())
                .trackId(course.getTrackId())
                .thumbnailUrl(course.getThumbnailUrl())
                .submittedAt(course.getSubmittedAt())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .moduleCount(modules.size())
                .lessonCount(lessonCount);
        if (includeModules) {
            builder.modules(modules.stream().map(m -> toModuleResponse(m, true)).toList());
        }
        return builder.build();
    }

    private ModuleResponse toModuleResponse(CourseModule module, boolean includeLessons) {
        ModuleResponse.ModuleResponseBuilder builder = ModuleResponse.builder()
                .id(module.getId())
                .courseId(module.getCourseId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .description(module.getDescription());
        if (includeLessons) {
            builder.lessons(lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId()).stream()
                    .map(l -> toLessonResponse(l, module.getCourseId(), null))
                    .toList());
        }
        return builder.build();
    }

    private LessonResponse toLessonResponse(Lesson lesson, Long courseId, String courseTitle) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModuleId())
                .courseId(courseId)
                .courseTitle(courseTitle)
                .title(lesson.getTitle())
                .type(lesson.getType())
                .durationMin(lesson.getDurationMin())
                .duration(formatDuration(lesson.getDurationMin()))
                .orderIndex(lesson.getOrderIndex())
                .contentUrl(lesson.getContentUrl())
                .readingContent(lesson.getReadingContent())
                .previewFree(lesson.isPreviewFree())
                .free(lesson.isPreviewFree())
                .summary(lesson.getSummary())
                .build();
    }

    private ResourceResponse toResourceResponse(LessonResource r) {
        return ResourceResponse.builder()
                .id(r.getId())
                .lessonId(r.getLessonId())
                .title(r.getTitle())
                .fileUrl(r.getFileUrl())
                .fileType(r.getFileType())
                .label(r.getTitle())
                .meta(r.getFileType())
                .type("download")
                .build();
    }

    private List<TranscriptLine> parseTranscriptLines(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<TranscriptLine> lines = new ArrayList<>();
        String[] parts = text.split("\n");
        int seconds = 0;
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            String t = formatTimestamp(seconds);
            lines.add(TranscriptLine.builder().t(t).seconds(seconds).text(trimmed).build());
            seconds += 5;
        }
        return lines;
    }

    private String formatTimestamp(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null) return "—";
        if (minutes < 60) return minutes + " min";
        return (minutes / 60) + " hr " + (minutes % 60) + " min";
    }

    private String toJson(List<String> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static Long longVal(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
