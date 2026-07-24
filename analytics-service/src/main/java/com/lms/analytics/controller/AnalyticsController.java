package com.lms.analytics.controller;

import com.lms.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/mentor/dashboard")
    public Map<String, Object> mentorDashboard(@RequestHeader("X-User-Id") Long mentorId) {
        return analyticsService.mentorDashboard(mentorId);
    }

    @GetMapping("/mentor/revenue")
    public Map<String, Object> mentorRevenue(
            @RequestHeader("X-User-Id") Long mentorId,
            @RequestParam(defaultValue = "week") String period) {
        return analyticsService.mentorRevenue(mentorId, period);
    }

    @GetMapping("/mentor/students")
    public Map<String, Object> mentorStudents(@RequestHeader("X-User-Id") Long mentorId) {
        return analyticsService.mentorStudents(mentorId);
    }

    @GetMapping("/mentor/courses/{courseId}")
    public Map<String, Object> mentorCourseAnalytics(
            @RequestHeader("X-User-Id") Long mentorId,
            @PathVariable Long courseId) {
        return analyticsService.mentorCourseAnalytics(mentorId, courseId);
    }

    @GetMapping("/admin/dashboard")
    public Map<String, Object> adminDashboard(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return analyticsService.adminDashboard();
    }

    @GetMapping("/admin/reports/enrollments")
    public List<Map<String, Object>> enrollmentReport(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(role);
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();
        return analyticsService.enrollmentReport(start, end);
    }

    @GetMapping("/admin/reports/revenue")
    public List<Map<String, Object>> revenueReport(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(role);
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();
        return analyticsService.revenueReport(start, end);
    }

    @GetMapping("/admin/reports/courses")
    public List<Map<String, Object>> courseReport(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(role);
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();
        return analyticsService.courseReport(start, end);
    }

    @GetMapping("/admin/export")
    public ResponseEntity<String> exportCsv(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "enrollments") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(role);
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();
        String csv = analyticsService.exportCsv(type, start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "-report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @GetMapping("/student/dashboard")
    public Map<String, Object> studentDashboard(@RequestHeader("X-User-Id") Long userId) {
        return analyticsService.studentDashboard(userId);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "analytics-service");
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
