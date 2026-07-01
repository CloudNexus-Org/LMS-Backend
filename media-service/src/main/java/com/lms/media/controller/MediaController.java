package com.lms.media.controller;

import com.lms.media.dto.MediaFileResponse;
import com.lms.media.dto.PresignedUrlResponse;
import com.lms.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse uploadImage(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestPart("file") MultipartFile file) {
        return mediaService.uploadImage(userId, file);
    }

    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse uploadVideo(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestPart("file") MultipartFile file) {
        return mediaService.uploadVideo(userId, role, file);
    }

    @PostMapping(value = "/upload/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse uploadDocument(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestPart("file") MultipartFile file) {
        return mediaService.uploadDocument(userId, file);
    }

    @PostMapping(value = "/upload/course-thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse uploadCourseThumbnail(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) Long courseId) {
        return mediaService.uploadCourseThumbnail(userId, role, file, courseId);
    }

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse uploadAvatar(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestPart("file") MultipartFile file) {
        return mediaService.uploadAvatar(userId, file);
    }

    @GetMapping("/files/{fileId}")
    public MediaFileResponse getFile(@PathVariable Long fileId) {
        return mediaService.getFile(fileId);
    }

    @DeleteMapping("/files/{fileId}")
    public MediaFileResponse deleteFile(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long fileId) {
        return mediaService.deleteFile(userId, role, fileId);
    }

    @GetMapping("/files/{fileId}/presigned-url")
    public PresignedUrlResponse presignedUrl(@PathVariable Long fileId) {
        return mediaService.getPresignedUrl(fileId);
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        Path path = mediaService.resolveDownloadPath(fileId);
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "media-service");
    }
}
