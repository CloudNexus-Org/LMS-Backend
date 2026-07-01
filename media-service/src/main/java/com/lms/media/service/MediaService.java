package com.lms.media.service;

import com.lms.media.config.MediaStorageProperties;
import com.lms.media.dto.MediaFileResponse;
import com.lms.media.dto.PresignedUrlResponse;
import com.lms.media.model.MediaFile;
import com.lms.media.repository.MediaFileRepository;
import com.lms.media.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private static final long IMAGE_MAX = 5L * 1024 * 1024;
    private static final long DOCUMENT_MAX = 50L * 1024 * 1024;
    private static final long VIDEO_MAX = 2L * 1024 * 1024 * 1024;

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp", "svg");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "webm", "mov");
    private static final Set<String> DOC_EXT = Set.of("pdf", "zip", "docx");

    private final MediaFileRepository mediaFileRepository;
    private final LocalStorageService storageService;
    private final MediaStorageProperties properties;

    @Transactional
    public MediaFileResponse uploadImage(Long userId, MultipartFile file) {
        return saveUpload(userId, file, "images", IMAGE_EXT, IMAGE_MAX, "image");
    }

    @Transactional
    public MediaFileResponse uploadVideo(Long userId, String role, MultipartFile file) {
        requireMentor(role);
        return saveUpload(userId, file, "lessons/videos", VIDEO_EXT, VIDEO_MAX, "video");
    }

    @Transactional
    public MediaFileResponse uploadDocument(Long userId, MultipartFile file) {
        return saveUpload(userId, file, "documents", DOC_EXT, DOCUMENT_MAX, "document");
    }

    @Transactional
    public MediaFileResponse uploadCourseThumbnail(Long userId, String role, MultipartFile file, Long courseId) {
        requireMentor(role);
        MediaFileResponse response = saveUpload(userId, file, "courses/" + (courseId != null ? courseId : "draft"), IMAGE_EXT, IMAGE_MAX, "course-thumbnail");
        if (courseId != null) {
            mediaFileRepository.findById(response.getId()).ifPresent(m -> {
                m.setEntityType("course");
                m.setEntityId(courseId);
                mediaFileRepository.save(m);
            });
        }
        return response;
    }

    @Transactional
    public MediaFileResponse uploadAvatar(Long userId, MultipartFile file) {
        MediaFileResponse response = saveUpload(userId, file, "avatars/" + userId, IMAGE_EXT, IMAGE_MAX, "avatar");
        mediaFileRepository.findById(response.getId()).ifPresent(m -> {
            m.setEntityType("avatar");
            m.setEntityId(userId);
            mediaFileRepository.save(m);
        });
        return response;
    }

    public MediaFileResponse getFile(Long fileId) {
        return toResponse(getActiveFile(fileId));
    }

    @Transactional
    public MediaFileResponse deleteFile(Long userId, String role, Long fileId) {
        MediaFile file = getActiveFile(fileId);
        if (!canDelete(userId, role, file)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this file");
        }
        file.setDeletedAt(Instant.now());
        mediaFileRepository.save(file);
        try {
            storageService.delete(file.getStorageKey());
        } catch (IOException ignored) {
        }
        return toResponse(file);
    }

    public PresignedUrlResponse getPresignedUrl(Long fileId) {
        MediaFile file = getActiveFile(fileId);
        Instant expires = Instant.now().plus(1, ChronoUnit.HOURS);
        String url = properties.getBaseUrl() + "/" + fileId + "/download?expires=" + expires.getEpochSecond();
        return PresignedUrlResponse.builder()
                .fileId(fileId)
                .url(url)
                .expiresAt(expires)
                .build();
    }

    public java.nio.file.Path resolveDownloadPath(Long fileId) {
        MediaFile file = getActiveFile(fileId);
        return storageService.resolve(file.getStorageKey());
    }

    private MediaFileResponse saveUpload(
            Long userId,
            MultipartFile file,
            String folder,
            Set<String> allowedExt,
            long maxBytes,
            String fileType) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds size limit");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = extension(original);
        if (!allowedExt.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed: " + ext);
        }
        try {
            LocalStorageService.StoredFile stored = storageService.store(file, folder);
            MediaFile entity = MediaFile.builder()
                    .fileName(original)
                    .fileType(fileType)
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storageKey(stored.storageKey())
                    .storageUrl(storageService.publicUrl(null))
                    .uploadedBy(userId)
                    .build();
            MediaFile saved = mediaFileRepository.save(entity);
            saved.setStorageUrl(storageService.publicUrl(saved.getId()));
            saved = mediaFileRepository.save(saved);
            return toResponse(saved);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }

    private MediaFile getActiveFile(Long fileId) {
        MediaFile file = mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        if (file.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return file;
    }

    private boolean canDelete(Long userId, String role, MediaFile file) {
        if (role != null && "ADMIN".equalsIgnoreCase(role)) return true;
        return userId != null && userId.equals(file.getUploadedBy());
    }

    private void requireMentor(String role) {
        if (role == null || !"MENTOR".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor access required");
        }
    }

    private MediaFileResponse toResponse(MediaFile file) {
        return MediaFileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .storageKey(file.getStorageKey())
                .storageUrl(file.getStorageUrl())
                .url(file.getStorageUrl())
                .uploadedBy(file.getUploadedBy())
                .entityType(file.getEntityType())
                .entityId(file.getEntityId())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private String extension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }
}
