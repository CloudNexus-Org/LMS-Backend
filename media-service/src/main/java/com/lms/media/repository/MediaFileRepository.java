package com.lms.media.repository;

import com.lms.media.model.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    List<MediaFile> findByUploadedByAndDeletedAtIsNull(Long uploadedBy);
}
