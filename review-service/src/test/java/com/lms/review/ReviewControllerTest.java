package com.lms.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"review.created", "review.updated"})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void courseSummary() throws Exception {
        mockMvc.perform(get("/api/reviews/courses/2/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(2))
                .andExpect(jsonPath("$.totalReviews").value(1));
    }

    @Test
    void submitReview() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "rating", 5,
                "title", "Great course",
                "body", "Learned a lot from this course.",
                "reviewerName", "Test Student"
        ));
        mockMvc.perform(post("/api/reviews/courses/3")
                        .header("X-User-Id", "301")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }
}
