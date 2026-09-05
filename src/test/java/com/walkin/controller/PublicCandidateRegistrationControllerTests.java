package com.walkin.controller;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.service.CandidateRegistrationService;
import com.walkin.service.HiringDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PublicCandidateRegistrationControllerTests {

    private static final UUID REFERENCE =
            UUID.fromString("6593f459-76b0-44b6-bc37-4147b87c8970");

    @Autowired
    private WebApplicationContext applicationContext;

    @MockitoBean
    private HiringDriveService driveService;

    @MockitoBean
    private CandidateRegistrationService registrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void candidateRegistersWithoutAuthenticationAndReceivesSafeAcknowledgement()
            throws Exception {
        CandidateRegistration registration = registration();
        when(registrationService.register(eq("valid-token"), any()))
                .thenReturn(registration);

        mockMvc.perform(multipart("/api/public/hiring-drives/valid-token/registrations")
                        .param("firstName", "Asha")
                        .param("email", "asha@example.com")
                        .param("notificationChannel", "EMAIL")
                        .param("notificationDestination", "alerts@example.com")
                        .param("advanceNoticeMinutes", "30"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationReference").value(REFERENCE.toString()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.registeredAt").value("2026-09-05T13:00:00Z"))
                .andExpect(jsonPath("$.firstName").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.notificationDestination").doesNotExist());

        verify(registrationService).register(eq("valid-token"), any());
    }

    @Test
    void candidateCanAttachPdfResume() throws Exception {
        CandidateRegistration registration = registration();
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", "%PDF-test".getBytes());
        when(registrationService.register(eq("valid-token"), any()))
                .thenReturn(registration);

        mockMvc.perform(multipart("/api/public/hiring-drives/valid-token/registrations")
                        .file(resume)
                        .param("firstName", "Asha")
                        .param("email", "asha@example.com")
                        .param("notificationChannel", "SMS")
                        .param("notificationDestination", "9876543210")
                        .param("advanceNoticeMinutes", "15"))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidNotificationPreferencesAreRejectedBeforeServiceCall() throws Exception {
        mockMvc.perform(multipart("/api/public/hiring-drives/valid-token/registrations")
                        .param("firstName", "Asha")
                        .param("email", "asha@example.com")
                        .param("advanceNoticeMinutes", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.notificationChannel").exists())
                .andExpect(jsonPath("$.fieldErrors.notificationDestination").exists())
                .andExpect(jsonPath("$.fieldErrors.advanceNoticeMinutes").exists());
    }

    private CandidateRegistration registration() {
        CandidateRegistration registration = org.mockito.Mockito.mock(CandidateRegistration.class);
        when(registration.getRegistrationReference()).thenReturn(REFERENCE);
        when(registration.getStatus()).thenReturn(CandidateRegistrationStatus.WAITING);
        when(registration.getRegisteredAt())
                .thenReturn(OffsetDateTime.parse("2026-09-05T13:00:00Z"));
        return registration;
    }
}
