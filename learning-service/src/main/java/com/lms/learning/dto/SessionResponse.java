package com.lms.learning.dto;

import com.lms.learning.model.LearningSession;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class SessionResponse {
    Long id;
    String trackId;
    Long lastLessonId;
    Integer lastPositionSec;
    String resumeUrl;
    Instant updatedAt;

    public static SessionResponse from(LearningSession session) {
        String resumeUrl = session.getTrackId() != null && session.getLastLessonId() != null
                ? "/learn/" + session.getTrackId() + "/lessons/" + session.getLastLessonId()
                : null;
        return SessionResponse.builder()
                .id(session.getId())
                .trackId(session.getTrackId())
                .lastLessonId(session.getLastLessonId())
                .lastPositionSec(session.getLastPositionSec())
                .resumeUrl(resumeUrl)
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
