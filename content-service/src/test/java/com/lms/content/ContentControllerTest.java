package com.lms.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/content/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void studentCanListTrackLessons() throws Exception {
        mockMvc.perform(get("/api/content/tracks/cloud/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void mentorCanCreateCourseAndCurriculum() throws Exception {
        String createBody = """
                {
                  "title": "Test Course for Integration",
                  "description": "A long enough description for the test course draft creation flow.",
                  "category": "Cloud & DevOps",
                  "level": "Beginner",
                  "language": "English",
                  "trackId": "cloud"
                }
                """;

        String response = mockMvc.perform(post("/api/content/courses")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "MENTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Course for Integration"))
                .andReturn().getResponse().getContentAsString();

        String courseId = response.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(post("/api/content/courses/" + courseId + "/modules")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Module 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Module 1"));

        mockMvc.perform(get("/api/content/courses/drafts")
                        .header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void seededLessonDetailWorks() throws Exception {
        mockMvc.perform(get("/api/content/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists());
    }
}
