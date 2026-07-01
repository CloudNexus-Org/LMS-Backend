package com.lms.enrollment.repository;

import com.lms.enrollment.model.TrackProgress;
import com.lms.enrollment.model.TrackProgressId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackProgressRepository extends JpaRepository<TrackProgress, TrackProgressId> {
    Optional<TrackProgress> findByIdUserIdAndIdTrackId(Long userId, String trackId);
    List<TrackProgress> findByIdUserId(Long userId);
}
