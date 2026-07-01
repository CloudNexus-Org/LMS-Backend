package com.lms.learning.seed;

import com.lms.learning.model.*;
import com.lms.learning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LearningSessionRepository sessionRepository;
    private final LessonQaRepository lessonQaRepository;

    private static final Long SEED_USER_ID = 3L;

    @Override
    @Transactional
    public void run(String... args) {
        if (noteRepository.count() > 0) {
            return;
        }

        noteRepository.saveAll(List.of(
                Note.builder()
                        .userId(SEED_USER_ID)
                        .lessonId(5L)
                        .trackId("cloud")
                        .content("EC2 instance types: t3 for burstable, m5 for general purpose workloads.")
                        .build(),
                Note.builder()
                        .userId(SEED_USER_ID)
                        .lessonId(12L)
                        .trackId("cloud")
                        .content("S3 lifecycle policies help reduce storage costs for infrequently accessed data.")
                        .build(),
                Note.builder()
                        .userId(SEED_USER_ID)
                        .lessonId(3L)
                        .trackId("ai")
                        .content("RAG pipeline: embed documents, store in vector DB, retrieve at query time.")
                        .build()
        ));

        bookmarkRepository.saveAll(List.of(
                Bookmark.builder()
                        .userId(SEED_USER_ID)
                        .lessonId(8L)
                        .trackId("cloud")
                        .title("VPC Networking Deep Dive")
                        .build(),
                Bookmark.builder()
                        .userId(SEED_USER_ID)
                        .lessonId(15L)
                        .trackId("ai")
                        .title("Azure OpenAI Fine-tuning")
                        .build()
        ));

        sessionRepository.save(LearningSession.builder()
                .userId(SEED_USER_ID)
                .trackId("cloud")
                .lastLessonId(27L)
                .lastPositionSec(420)
                .build());

        lessonQaRepository.save(LessonQa.builder()
                .lessonId(5L)
                .userId(SEED_USER_ID)
                .question("What is the difference between t3 and m5 instances?")
                .answer("t3 instances are burstable and cost-effective for variable workloads; m5 provides consistent performance.")
                .answeredBy(2L)
                .build());
    }
}
