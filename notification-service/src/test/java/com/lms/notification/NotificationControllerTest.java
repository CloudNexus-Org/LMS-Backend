package com.lms.notification;

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
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void studentCanListNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/me")
                        .header("X-User-Id", "3")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void unreadCountWorks() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-User-Id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    void preferencesRoundTrip() throws Exception {
        mockMvc.perform(get("/api/notifications/preferences")
                        .header("X-User-Id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailEnrollment").value(true));

        mockMvc.perform(put("/api/notifications/preferences")
                        .header("X-User-Id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailEnrollment\":false,\"pushEnabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailEnrollment").value(false));
    }

    @Test
    void adminCanBroadcast() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "broadcast": false,
                                  "userIds": [3],
                                  "type": "system",
                                  "title": "Test broadcast",
                                  "message": "Hello from test",
                                  "link": "/student/notifications",
                                  "actionLabel": "Open"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test broadcast"));
    }
}
