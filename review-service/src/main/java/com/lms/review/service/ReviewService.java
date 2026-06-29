package com.lms.review.service;

import com.lms.review.dto.ReviewRequest;
import com.lms.review.dto.ReviewResponse;
import com.lms.review.event.ReviewEventProducer;
import com.lms.review.model.Review;
import com.lms.review.model.ReviewHelpfulVote;
import com.lms.review.repository.ReviewHelpfulVoteRepository;
import com.lms.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;
    private final ReviewEventProducer eventProducer;

    public Map<String, Object> listCourseReviews(Long courseId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(
                courseId, PageRequest.of(page, size));
        return Map.of(
                "content", reviews.getContent().stream().map(ReviewResponse::from).toList(),
                "page", reviews.getNumber(),
                "size", reviews.getSize(),
                "totalElements", reviews.getTotalElements(),
                "totalPages", reviews.getTotalPages()
        );
    }

    @Transactional
    public ReviewResponse submitReview(Long courseId, Long userId, ReviewRequest request) {
        validateReviewRequest(request);
        if (reviewRepository.findByCourseIdAndUserId(courseId, userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this course");
        }
        Review review = Review.builder()
                .courseId(courseId)
                .userId(userId)
                .reviewerName(request.getReviewerName() != null ? request.getReviewerName() : "Student")
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .helpfulCount(0)
                .build();
        review = reviewRepository.save(review);
        publishRatingEvent(courseId, review.getId(), true);
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your review");
        }
        validateReviewRequest(request);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setBody(request.getBody());
        review = reviewRepository.save(review);
        publishRatingEvent(review.getCourseId(), review.getId(), false);
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your review");
        }
        Long courseId = review.getCourseId();
        reviewRepository.delete(review);
        publishRatingEvent(courseId, reviewId, false);
    }

    @Transactional
    public ReviewResponse markHelpful(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (helpfulVoteRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            return ReviewResponse.from(review);
        }
        helpfulVoteRepository.save(ReviewHelpfulVote.builder().reviewId(reviewId).userId(userId).build());
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    public List<ReviewResponse> myReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public Map<String, Object> courseSummary(Long courseId) {
        List<Review> reviews = reviewRepository.findByCourseIdOrderByCreatedAtDesc(
                courseId, PageRequest.of(0, 1000)).getContent();
        if (reviews.isEmpty()) {
            return Map.of(
                    "courseId", courseId,
                    "avgRating", 0.0,
                    "totalReviews", 0,
                    "distribution", Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0)
            );
        }
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        Map<Integer, Integer> dist = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            dist.put(i, distribution.getOrDefault(i, 0L).intValue());
        }
        return Map.of(
                "courseId", courseId,
                "avgRating", Math.round(avg * 10.0) / 10.0,
                "totalReviews", reviews.size(),
                "distribution", dist
        );
    }

    public List<ReviewResponse> mentorReviews(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return reviewRepository.findByCourseIdIn(courseIds).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    private void validateReviewRequest(ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
    }

    private void publishRatingEvent(Long courseId, Long reviewId, boolean created) {
        List<Review> all = reviewRepository.findByCourseIdOrderByCreatedAtDesc(
                courseId, PageRequest.of(0, 10000)).getContent();
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(0);
        int count = all.size();
        if (created) {
            eventProducer.publishReviewCreated(courseId, Math.round(avg * 10.0) / 10.0, count, reviewId);
        } else {
            eventProducer.publishReviewUpdated(courseId, Math.round(avg * 10.0) / 10.0, count, reviewId);
        }
    }
}
