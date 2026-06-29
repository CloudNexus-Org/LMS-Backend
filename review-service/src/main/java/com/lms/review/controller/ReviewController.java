package com.lms.review.controller;

import com.lms.review.dto.ReviewRequest;
import com.lms.review.dto.ReviewResponse;
import com.lms.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/courses/{courseId}")
    public Map<String, Object> courseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.listCourseReviews(courseId, page, size);
    }

    @PostMapping("/courses/{courseId}")
    public ReviewResponse submitReview(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReviewRequest request) {
        return reviewService.submitReview(courseId, userId, request);
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReviewRequest request) {
        return reviewService.updateReview(reviewId, userId, request);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId) {
        reviewService.deleteReview(reviewId, userId);
    }

    @PostMapping("/{reviewId}/helpful")
    public ReviewResponse markHelpful(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId) {
        return reviewService.markHelpful(reviewId, userId);
    }

    @GetMapping("/me")
    public List<ReviewResponse> myReviews(@RequestHeader("X-User-Id") Long userId) {
        return reviewService.myReviews(userId);
    }

    @GetMapping("/courses/{courseId}/summary")
    public Map<String, Object> courseSummary(@PathVariable Long courseId) {
        return reviewService.courseSummary(courseId);
    }

    @GetMapping("/mentor/me")
    public List<ReviewResponse> mentorReviews(@RequestParam List<Long> courseIds) {
        return reviewService.mentorReviews(courseIds);
    }
}
