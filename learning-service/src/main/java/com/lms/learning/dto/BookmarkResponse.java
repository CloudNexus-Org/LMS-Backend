package com.lms.learning.dto;

import com.lms.learning.model.Bookmark;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class BookmarkResponse {
    Long id;
    Long lessonId;
    String trackId;
    String title;
    Instant createdAt;

    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .lessonId(bookmark.getLessonId())
                .trackId(bookmark.getTrackId())
                .title(bookmark.getTitle())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
