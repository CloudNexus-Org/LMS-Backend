package com.lms.user.controller;

import com.lms.user.dto.*;
import com.lms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ProfileResponse getProfile(@RequestHeader("X-User-Id") Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(userId, request);
    }

    @PatchMapping("/profile/avatar")
    public ProfileResponse updateAvatar(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AvatarUpdateRequest request) {
        return userService.updateAvatar(userId, request);
    }

    @GetMapping("/profile/settings")
    public SettingsResponse getSettings(@RequestHeader("X-User-Id") Long userId) {
        return userService.getSettings(userId);
    }

    @PutMapping("/profile/settings")
    public SettingsResponse updateSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SettingsUpdateRequest request) {
        return userService.updateSettings(userId, request);
    }

    @GetMapping("/mentors")
    public List<MentorListItemResponse> listMentors(@RequestHeader("X-User-Role") String role) {
        userService.requireAdmin(role);
        return userService.listMentors();
    }

    @PostMapping("/mentors")
    public MentorListItemResponse createMentor(
            @RequestHeader("X-User-Role") String role,
            @RequestBody CreateMentorRequest request) {
        userService.requireAdmin(role);
        return userService.createMentor(request);
    }

    @GetMapping("/students/{studentId}/summary")
    public StudentSummaryResponse studentSummary(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long studentId) {
        userService.requireMentorOrAdmin(role);
        return userService.getStudentSummary(studentId);
    }

    @GetMapping
    public PagedResponse<AdminUserResponse> listUsers(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roleFilter,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        userService.requireAdmin(role);
        return userService.listUsers(search, roleFilter, status, page, size);
    }

    @PostMapping
    public AdminUserResponse createUser(
            @RequestHeader("X-User-Role") String role,
            @RequestBody CreateUserRequest request) {
        userService.requireAdmin(role);
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public AdminUserResponse getUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        userService.requireAdmin(role);
        return userService.getUser(userId);
    }

    @PutMapping("/{userId}")
    public AdminUserResponse updateUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        userService.requireAdmin(role);
        return userService.updateUser(userId, request);
    }

    @PatchMapping("/{userId}/status")
    public AdminUserResponse updateStatus(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request) {
        userService.requireAdmin(role);
        return userService.updateStatus(userId, request);
    }

    @DeleteMapping("/{userId}")
    public Map<String, String> deleteUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId) {
        userService.requireAdmin(role);
        userService.deleteUser(userId);
        return Map.of("status", "deleted", "userId", String.valueOf(userId));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "user-service");
    }
}
