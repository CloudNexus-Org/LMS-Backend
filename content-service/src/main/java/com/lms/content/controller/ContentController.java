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
            @RequestBody CreateCourseRequest request) {
        return contentService.createCourse(userId, request);
    }

    @PutMapping("/courses/{courseId}")
    public CourseResponse updateCourse(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @RequestBody UpdateCourseRequest request) {
        return contentService.updateCourse(userId, courseId, request);
    }

    @GetMapping("/courses/{courseId}")
    public CourseResponse getCourse(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return contentService.getCourse(userId, courseId);
    }

    @GetMapping("/courses/drafts")
    public List<CourseResponse> listDrafts(@RequestHeader("X-User-Id") Long userId) {
        return contentService.listDrafts(userId);
    }

    @PostMapping("/courses/{courseId}/modules")
    public ModuleResponse addModule(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @RequestBody ModuleRequest request) {
        return contentService.addModule(userId, courseId, request);
    }

    @PutMapping("/courses/{courseId}/modules/{moduleId}")
    public ModuleResponse updateModule(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestBody ModuleRequest request) {
        return contentService.updateModule(userId, courseId, moduleId, request);
    }

    @DeleteMapping("/courses/{courseId}/modules/{moduleId}")
    public Map<String, String> deleteModule(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId) {
        return contentService.deleteModule(userId, courseId, moduleId);
    }

    @PostMapping("/courses/{courseId}/modules/{moduleId}/lessons")
    public LessonResponse addLesson(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestBody LessonRequest request) {
        return contentService.addLesson(userId, courseId, moduleId, request);
    }

    @PutMapping("/courses/{courseId}/lessons/{lessonId}")
    public LessonResponse updateLesson(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @RequestBody LessonRequest request) {
        return contentService.updateLesson(userId, courseId, lessonId, request);
    }

    @DeleteMapping("/courses/{courseId}/lessons/{lessonId}")
    public Map<String, String> deleteLesson(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        return contentService.deleteLesson(userId, courseId, lessonId);
    }

    @PutMapping("/courses/{courseId}/curriculum/reorder")
    public CourseResponse reorderCurriculum(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @RequestBody ReorderRequest request) {
        return contentService.reorderCurriculum(userId, courseId, request);
    }

    @PostMapping("/courses/{courseId}/submit-for-approval")
    public CourseResponse submitForApproval(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return contentService.submitForApproval(userId, courseId);
    }

    @PatchMapping("/courses/{courseId}/pricing")
    public CourseResponse updatePricing(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId,
            @RequestBody PricingRequest request) {
        return contentService.updatePricing(userId, courseId, request);
    }

    @PostMapping("/courses/{courseId}/publish")
    public CourseResponse publishCourse(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return contentService.publishCourse(userId, courseId);
    }

    @GetMapping("/tracks/{trackId}/lessons")
    public List<LessonResponse> getTrackLessons(@PathVariable String trackId) {
        return contentService.getTrackLessons(trackId);
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
