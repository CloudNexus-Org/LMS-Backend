package com.lms.content.controller;

import com.lms.content.dto.*;
import com.lms.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/courses")
    public CourseResponse createCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody CreateCourseRequest request) {
        return contentService.createCourse(userId, role, request);
    }

    @PutMapping("/courses/{courseId}")
    public CourseResponse updateCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestBody UpdateCourseRequest request) {
        return contentService.updateCourse(userId, role, courseId, request);
    }

    @DeleteMapping("/courses/{courseId}")
    public Map<String, String> deleteCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId) {
        return contentService.deleteCourse(userId, role, courseId);
    }

    @GetMapping("/courses/{courseId}")
    public CourseResponse getCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId) {
        return contentService.getCourse(userId, role, courseId);
    }

    @GetMapping("/courses/drafts")
    public List<CourseResponse> listDrafts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        return contentService.listDrafts(userId, role);
    }

    @PostMapping("/courses/{courseId}/modules")
    public ModuleResponse addModule(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestBody ModuleRequest request) {
        return contentService.addModule(userId, role, courseId, request);
    }

    @PutMapping("/courses/{courseId}/modules/{moduleId}")
    public ModuleResponse updateModule(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestBody ModuleRequest request) {
        return contentService.updateModule(userId, role, courseId, moduleId, request);
    }

    @DeleteMapping("/courses/{courseId}/modules/{moduleId}")
    public Map<String, String> deleteModule(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @PathVariable Long moduleId) {
        return contentService.deleteModule(userId, role, courseId, moduleId);
    }

    @PostMapping("/courses/{courseId}/modules/{moduleId}/lessons")
    public LessonResponse addLesson(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestBody LessonRequest request) {
        return contentService.addLesson(userId, role, courseId, moduleId, request);
    }

    @PutMapping("/courses/{courseId}/lessons/{lessonId}")
    public LessonResponse updateLesson(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @RequestBody LessonRequest request) {
        return contentService.updateLesson(userId, role, courseId, lessonId, request);
    }

    @DeleteMapping("/courses/{courseId}/lessons/{lessonId}")
    public Map<String, String> deleteLesson(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        return contentService.deleteLesson(userId, role, courseId, lessonId);
    }

    @PutMapping("/courses/{courseId}/curriculum/reorder")
    public CourseResponse reorderCurriculum(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestBody ReorderRequest request) {
        return contentService.reorderCurriculum(userId, role, courseId, request);
    }

    @PostMapping("/courses/{courseId}/submit-for-approval")
    public CourseResponse submitForApproval(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId) {
        return contentService.submitForApproval(userId, role, courseId);
    }

    @PatchMapping("/courses/{courseId}/pricing")
    public CourseResponse updatePricing(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestBody PricingRequest request) {
        return contentService.updatePricing(userId, role, courseId, request);
    }

    @PostMapping("/courses/{courseId}/publish")
    public CourseResponse publishCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId) {
        return contentService.publishCourse(userId, role, courseId);
    }

    @GetMapping("/tracks/{trackId}/lessons")
    public List<LessonResponse> getTrackLessons(@PathVariable String trackId) {
        return contentService.getTrackLessons(trackId);
    }

    @GetMapping("/catalog-courses/{catalogCourseId}/lessons")
    public List<LessonResponse> getCatalogCourseLessons(@PathVariable Long catalogCourseId) {
        return contentService.getCatalogCourseLessons(catalogCourseId);
    }

    @GetMapping("/lessons/{lessonId}")
    public LessonResponse getLesson(@PathVariable Long lessonId) {
        return contentService.getLessonDetail(lessonId);
    }

    @GetMapping("/lessons/{lessonId}/resources")
    public List<ResourceResponse> getLessonResources(@PathVariable Long lessonId) {
        return contentService.getLessonResources(lessonId);
    }

    @GetMapping("/lessons/{lessonId}/transcript")
    public TranscriptResponse getLessonTranscript(@PathVariable Long lessonId) {
        return contentService.getLessonTranscript(lessonId);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "content-service");
    }
}
