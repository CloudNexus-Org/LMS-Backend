package com.lms.admin.controller;

import com.lms.admin.dto.*;
import com.lms.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/approvals/courses")
    public List<CourseApprovalResponse> listApprovals(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String status) {
        adminService.requireAdmin(role);
        return adminService.listApprovals(status);
    }

    @GetMapping("/approvals/courses/{courseId}")
    public CourseApprovalResponse getApproval(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable String courseId) {
        adminService.requireAdmin(role);
        return adminService.getApproval(courseId);
    }

    @PostMapping("/approvals/courses/{courseId}/approve")
    public CourseApprovalResponse approveCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable String courseId) {
        adminService.requireAdmin(role);
        return adminService.approveCourse(courseId, userId);
    }

    @PostMapping("/approvals/courses/{courseId}/reject")
    public CourseApprovalResponse rejectCourse(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable String courseId,
            @RequestBody RejectCourseRequest request) {
        adminService.requireAdmin(role);
        return adminService.rejectCourse(courseId, userId, request.getReason());
    }

    @GetMapping("/financials/summary")
    public FinancialSummaryResponse financialSummary(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        adminService.requireAdmin(role);
        return adminService.getFinancialSummary();
    }

    @GetMapping("/financials/transactions")
    public List<TransactionResponse> listTransactions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        adminService.requireAdmin(role);
        return adminService.listTransactions(type, search);
    }

    @GetMapping("/financials/payouts")
    public List<MentorPayoutResponse> listPayouts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        adminService.requireAdmin(role);
        return adminService.listPayouts();
    }

    @PostMapping("/financials/payouts")
    public MentorPayoutResponse processPayout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody ProcessPayoutRequest request) {
        adminService.requireAdmin(role);
        return adminService.processPayout(request, userId);
    }

    @GetMapping("/settings")
    public SettingsResponse getSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        adminService.requireAdmin(role);
        return adminService.getSettings();
    }

    @PutMapping("/settings")
    public SettingsResponse updateSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody UpdateSettingsRequest request) {
        adminService.requireAdmin(role);
        return adminService.updateSettings(request, userId);
    }

    @GetMapping("/reports/platform")
    public PlatformReportResponse platformReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        adminService.requireAdmin(role);
        return adminService.getPlatformReport();
    }

    @PostMapping("/reports/generate")
    public PlatformReportResponse generateReport(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody GenerateReportRequest request) {
        adminService.requireAdmin(role);
        return adminService.generateReport(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "admin-service");
    }
}
