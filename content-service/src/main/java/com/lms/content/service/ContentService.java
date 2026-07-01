package com.lms.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.content.dto.*;
import com.lms.content.event.ContentEventPublisher;
import com.lms.content.model.*;
import com.lms.content.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional
    public CourseResponse createCourse(Long mentorId, CreateCourseRequest request) {
        requireMentor(mentorId);
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
    public CourseResponse updateCourse(Long mentorId, Long courseId, UpdateCourseRequest request) {
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

    public CourseResponse getCourse(Long mentorId, Long courseId) {
        CourseContent course = getOwnedCourse(mentorId, courseId);
        return toCourseResponse(course, true);
    }

    public List<CourseResponse> listDrafts(Long mentorId) {
        return courseRepository.findByMentorIdOrderByUpdatedAtDesc(mentorId).stream()
                .map(c -> toCourseResponse(c, false))
                .toList();
    }

    @Transactional
    public ModuleResponse addModule(Long mentorId, Long courseId, ModuleRequest request) {
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
    public ModuleResponse updateModule(Long mentorId, Long courseId, Long moduleId, ModuleRequest request) {
        getOwnedCourse(mentorId, courseId);
        CourseModule module = getModuleInCourse(courseId, moduleId);
        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());
        return toModuleResponse(moduleRepository.save(module), true);
    }

    @Transactional
    public Map<String, String> deleteModule(Long mentorId, Long courseId, Long moduleId) {
        getOwnedCourse(mentorId, courseId);
        CourseModule module = getModuleInCourse(courseId, moduleId);
        lessonRepository.deleteByModuleId(moduleId);
        moduleRepository.delete(module);
        return Map.of("message", "Module deleted");
    }

    @Transactional
    public LessonResponse addLesson(Long mentorId, Long courseId, Long moduleId, LessonRequest request) {
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
    public LessonResponse updateLesson(Long mentorId, Long courseId, Long lessonId, LessonRequest request) {
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
    public Map<String, String> deleteLesson(Long mentorId, Long courseId, Long lessonId) {
        getOwnedCourse(mentorId, courseId);
        Lesson lesson = getLessonInCourse(courseId, lessonId);
        lessonRepository.delete(lesson);
        return Map.of("message", "Lesson deleted");
    }

    @Transactional
    public CourseResponse reorderCurriculum(Long mentorId, Long courseId, ReorderRequest request) {
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
    public CourseResponse submitForApproval(Long mentorId, Long courseId) {
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course title is required");
        }
        course.setStatus(CourseStatus.PENDING);
        course.setSubmittedAt(Instant.now());
        CourseContent saved = courseRepository.save(course);
        eventPublisher.publishCourseSubmitted(saved);
        return toCourseResponse(saved, true);
    }

    @Transactional
    public CourseResponse updatePricing(Long mentorId, Long courseId, PricingRequest request) {
        CourseContent course = getOwnedCourse(mentorId, courseId);
        if (request.getPricingPlan() != null) course.setPricingPlan(request.getPricingPlan());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        return toCourseResponse(courseRepository.save(course), true);
    }

    @Transactional
    public CourseResponse publishCourse(Long mentorId, Long courseId) {
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

    public List<LessonResponse> getTrackLessons(String trackId) {
        List<CourseContent> courses = courseRepository.findByTrackId(trackId);
        if (courses.isEmpty()) {
            courses = courseRepository.findAll().stream()
                    .filter(c -> c.getStatus() == CourseStatus.PUBLISHED || c.getStatus() == CourseStatus.APPROVED)
                    .collect(Collectors.toList());
        }
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
        Long contentId = longVal(event.get("contentId"));
        if (contentId == null) contentId = longVal(event.get("courseId"));
        if (contentId == null) return;
        courseRepository.findById(contentId).ifPresent(course -> {
            course.setStatus(CourseStatus.APPROVED);
            courseRepository.save(course);
        });
    }

    @Transactional
    public void handleCourseRejected(Map<String, Object> event) {
        Long contentId = longVal(event.get("contentId"));
        if (contentId == null) contentId = longVal(event.get("courseId"));
        if (contentId == null) return;
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

    private void requireMentor(Long mentorId) {
        if (mentorId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id required");
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
