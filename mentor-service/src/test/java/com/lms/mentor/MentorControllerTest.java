package com.lms.mentor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"mentor.created", "mentor.profile-updated"})
class MentorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listMentors() throws Exception {
        mockMvc.perform(get("/api/mentors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMentorBySlug() throws Exception {
        mockMvc.perform(get("/api/mentors/arjan-singh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("arjan-singh"))
                .andExpect(jsonPath("$.name").value("Arjan Singh"));
    }
}
