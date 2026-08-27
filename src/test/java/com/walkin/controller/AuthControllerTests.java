package com.walkin.controller;

import com.walkin.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @ActiveProfiles("test")
class AuthControllerTests {
    @Autowired WebApplicationContext context;
    @Autowired ApplicationUserRepository users;
    @Autowired PasswordEncoder encoder;
    MockMvc mockMvc;
    @BeforeEach void setUp() { mockMvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    @Test void bootstrapAdminIsStoredWithBcryptNotPlainText() {
        var admin=users.findByUsernameIgnoreCase("test-admin").orElseThrow();
        assertNotEquals("test-password", admin.getPasswordHash()); assertTrue(encoder.matches("test-password", admin.getPasswordHash()));
    }
    @Test void validLoginReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test-admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty()).andExpect(jsonPath("$.expiresAt").exists());
    }
    @Test void invalidLoginReturnsUnauthorizedWithoutLeakingDetails() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test-admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
    @Test void recruiterCanReadButCannotModify() throws Exception {
        var recruiter=jwt().jwt(j -> j.subject("recruiter").claim("roles", "ROLE_RECRUITER"))
                .authorities(() -> "ROLE_RECRUITER");
        mockMvc.perform(get("/api/students").with(recruiter)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/students/1").with(recruiter)).andExpect(status().isForbidden());
    }
}
