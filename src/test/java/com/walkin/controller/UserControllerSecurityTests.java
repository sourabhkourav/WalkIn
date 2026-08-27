package com.walkin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @ActiveProfiles("test")
class UserControllerSecurityTests {
    @Autowired WebApplicationContext context;
    MockMvc mockMvc;
    @BeforeEach void setUp() { mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    @Test void recruiterCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/users").with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isForbidden());
    }

    @Test void administratorCanCreateRecruiterWithoutExposingPasswordHash() throws Exception {
        mockMvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"api-recruiter\",\"password\":\"strong-password-123\",\"role\":\"RECRUITER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("api-recruiter"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test void shortPasswordFailsValidation() throws Exception {
        mockMvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"another-user\",\"password\":\"short\",\"role\":\"RECRUITER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
