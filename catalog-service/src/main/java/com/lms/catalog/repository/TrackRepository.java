package com.lms.catalog.repository;

import com.lms.catalog.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, String> {

    Optional<Track> findBySlug(String slug);
}
