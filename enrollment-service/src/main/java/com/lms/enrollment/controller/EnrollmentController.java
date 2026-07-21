package com.lms.enrollment.controller;

import com.lms.enrollment.dto.*;
import com.lms.enrollment.model.LessonProgress;
import com.lms.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public EnrollmentDetailResponse enroll(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody EnrollRequest request) {
        return enrollmentService.enroll(userId, request);
    }

    @GetMapping("/me")
    public List<MyCourseResponse> myEnrollments(@RequestHeader("X-User-Id") Long userId) {
        return enrollmentService.myEnrollments(userId);
    }

    @GetMapping("/me/{trackId}")
    public EnrollmentDetailResponse myEnrollmentByTrack(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String trackId) {
        return enrollmentService.myEnrollmentByTrack(userId, trackId);
    }

    @GetMapping("/me/courses/{courseId}/progress")
    public CourseProgressResponse courseProgress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return enrollmentService.courseProgress(userId, courseId);
    }

    @PutMapping("/progress/lessons/{lessonId}")
    public LessonProgress updateLessonProgress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long lessonId,
            @RequestBody LessonProgressRequest request) {
        return enrollmentService.updateLessonProgress(userId, lessonId, request);
    }

    @GetMapping("/progress/tracks/{trackId}")
    public TrackProgressResponse trackProgress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String trackId) {
        return enrollmentService.trackProgress(userId, trackId);
    }

    @PostMapping("/progress/lessons/{lessonId}/complete")
    public TrackProgressResponse completeLesson(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long lessonId,
            @RequestBody CompleteLessonRequest request) {
        return enrollmentService.completeLesson(userId, lessonId, request);
    }

    @PostMapping("/progress/tracks/{trackId}/finish")
    public TrackProgressResponse finishTrack(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String trackId) {
        return enrollmentService.finishTrack(userId, trackId);
    }

    @GetMapping("/dashboard/student")
    public StudentDashboardResponse studentDashboard(@RequestHeader("X-User-Id") Long userId) {
        return enrollmentService.studentDashboard(userId);
    }

    @GetMapping("/check/{trackId}")
    public EnrollmentCheckResponse checkEnrollment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String trackId) {
        return enrollmentService.checkEnrollment(userId, trackId);
    }

    /** Active enrollment count for one catalog course (excludes CANCELLED). */
    @GetMapping("/courses/{courseId}/count")
    public Map<String, Long> courseEnrollmentCount(@PathVariable Long courseId) {
        return Map.of("courseId", courseId, "count", enrollmentService.activeEnrollmentCount(courseId));
    }

    /** Batch enrollment counts: ?courseIds=1,2,3 */
    @GetMapping("/counts")
    public Map<Long, Long> courseEnrollmentCounts(@RequestParam("courseIds") String courseIds) {
        List<Long> ids = Arrays.stream(courseIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return enrollmentService.activeEnrollmentCounts(ids);
    }

    @DeleteMapping("/{enrollmentId}")
    public void cancelEnrollment(
            @PathVariable Long enrollmentId,
            @RequestHeader("X-User-Role") String role) {
        enrollmentService.cancelEnrollment(enrollmentId, role);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "enrollment-service");
    }
}
