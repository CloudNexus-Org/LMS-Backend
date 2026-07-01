package com.lms.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MediaFileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String mimeType;
    private Long sizeBytes;
    private String storageKey;
    private String storageUrl;
    private String url;
    private Long uploadedBy;
    private String entityType;
    private Long entityId;
    private Instant createdAt;
}
