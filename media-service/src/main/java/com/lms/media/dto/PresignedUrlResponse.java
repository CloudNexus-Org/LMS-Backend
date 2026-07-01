package com.lms.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PresignedUrlResponse {
    private Long fileId;
    private String url;
    private Instant expiresAt;
}
