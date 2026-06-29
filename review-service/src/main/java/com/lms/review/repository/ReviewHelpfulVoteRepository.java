package com.lms.review.repository;

import com.lms.review.model.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, Long> {
    Optional<ReviewHelpfulVote> findByReviewIdAndUserId(Long reviewId, Long userId);
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}
