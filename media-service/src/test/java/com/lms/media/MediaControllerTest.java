package com.lms.media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/media/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void imageUploadRoundTrip() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00});

        String response = mockMvc.perform(multipart("/api/media/upload/image")
                        .file(file)
                        .header("X-User-Id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").value("avatar.png"))
                .andReturn().getResponse().getContentAsString();

        String fileId = response.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(get("/api/media/files/" + fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileType").value("image"));

        mockMvc.perform(get("/api/media/files/" + fileId + "/presigned-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void mentorCanUploadCourseThumbnail() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "thumb.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01});

        mockMvc.perform(multipart("/api/media/upload/course-thumbnail")
                        .file(file)
                        .param("courseId", "1")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileType").value("course-thumbnail"));
    }
}
