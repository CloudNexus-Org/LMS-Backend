package com.lms.review.seed;

import com.lms.review.model.Review;
import com.lms.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) {
        if (reviewRepository.count() > 0) return;

        reviewRepository.saveAll(List.of(
                Review.builder()
                        .courseId(2L)
                        .userId(201L)
                        .reviewerName("Priya Verma")
                        .rating(5)
                        .title("Excellent deep dive into Azure AI")
                        .body("Clear explanations on RAG pipelines and Azure OpenAI. Projects felt production-ready.")
                        .helpfulCount(12)
                        .build(),
                Review.builder()
                        .courseId(1L)
                        .userId(202L)
                        .reviewerName("Aarav Sharma")
                        .rating(5)
                        .title("Best AWS course I've taken")
                        .body("Structured labs and real-world architecture patterns. Highly recommend.")
                        .helpfulCount(8)
                        .build(),
                Review.builder()
                        .courseId(5L)
                        .userId(201L)
                        .reviewerName("Priya Verma")
                        .rating(4)
                        .title("Solid data engineering foundation")
                        .body("Great Airflow and PySpark modules. Would love more advanced Spark tuning.")
                        .helpfulCount(5)
                        .build()
        ));
    }
}
