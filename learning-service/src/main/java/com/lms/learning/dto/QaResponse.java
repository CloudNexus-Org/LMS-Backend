package com.lms.learning.dto;

import com.lms.learning.model.LessonQa;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class QaResponse {
    Long id;
    Long lessonId;
    Long userId;
    String question;
    String answer;
    Long answeredBy;
    Instant createdAt;

    public static QaResponse from(LessonQa qa) {
        return QaResponse.builder()
                .id(qa.getId())
                .lessonId(qa.getLessonId())
                .userId(qa.getUserId())
                .question(qa.getQuestion())
                .answer(qa.getAnswer())
                .answeredBy(qa.getAnsweredBy())
                .createdAt(qa.getCreatedAt())
                .build();
    }
}
