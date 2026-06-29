package com.lms.mentor.controller;

import com.lms.mentor.dto.MentorProfileUpdateRequest;
import com.lms.mentor.dto.MentorResponse;
import com.lms.mentor.model.MentorStudent;
import com.lms.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public List<MentorResponse> listMentors() {
        return mentorService.listMentors();
    }

    @GetMapping("/{slug}")
    public MentorResponse getMentor(@PathVariable String slug) {
        return mentorService.getBySlug(slug);
    }

    @GetMapping("/{slug}/courses")
    public List<Map<String, Object>> mentorCourses(@PathVariable String slug) {
        return mentorService.getMentorCourses(slug);
    }

    @GetMapping("/{slug}/reviews")
    public Map<String, Object> mentorReviews(@PathVariable String slug) {
        return mentorService.getMentorReviewsSummary(slug);
    }

    @GetMapping("/me/dashboard")
    public Map<String, Object> dashboard(@RequestHeader("X-User-Id") Long userId) {
        return mentorService.getDashboard(userId);
    }

    @GetMapping("/me/profile")
    public MentorResponse myProfile(@RequestHeader("X-User-Id") Long userId) {
        return mentorService.getMyProfile(userId);
    }

    @PutMapping("/me/profile")
    public MentorResponse updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody MentorProfileUpdateRequest request) {
        return mentorService.updateProfile(userId, request);
    }

    @GetMapping("/me/students")
    public List<MentorStudent> students(@RequestHeader("X-User-Id") Long userId) {
        return mentorService.getStudents(userId);
    }

    @GetMapping("/me/students/{studentId}")
    public MentorStudent studentDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long studentId) {
        return mentorService.getStudent(userId, studentId);
    }

    @GetMapping("/me/notifications-count")
    public Map<String, Integer> notificationsCount() {
        return mentorService.getNotificationsCount();
    }
}
