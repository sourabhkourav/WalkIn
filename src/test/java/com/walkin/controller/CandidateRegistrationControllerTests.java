package com.walkin.controller;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.service.CandidateRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class CandidateRegistrationControllerTests {

    private static final UUID REFERENCE =
            UUID.fromString("6593f459-76b0-44b6-bc37-4147b87c8970");

    @Autowired
    private WebApplicationContext applicationContext;

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
    void queueRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/hiring-drives/12/registrations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruiterCanFilterQueueWithoutSeeingNotificationDestinationOrResume() throws Exception {
        CandidateRegistration registration = registration();
        PageRequest pageable = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("registeredAt").ascending());
        when(registrationService.getRegistrations(
                eq(12), eq(CandidateRegistrationStatus.WAITING), eq("Asha"), any()))
                .thenReturn(new PageImpl<>(List.of(registration), pageable, 1));

        mockMvc.perform(get("/api/hiring-drives/12/registrations")
                        .param("status", "WAITING")
                        .param("query", "Asha")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].registrationReference")
                        .value(REFERENCE.toString()))
                .andExpect(jsonPath("$.content[0].firstName").value("Asha"))
                .andExpect(jsonPath("$.content[0].resumeAvailable").value(true))
                .andExpect(jsonPath("$.content[0].notificationDestination").doesNotExist())
                .andExpect(jsonPath("$.content[0].notificationChannel").doesNotExist())
                .andExpect(jsonPath("$.content[0].resume").doesNotExist());
    }

    @Test
    void recruiterCanMoveCandidateToCalledWithAuthenticatedIdentity() throws Exception {
        CandidateRegistration registration = registration();
        registration.setStatus(CandidateRegistrationStatus.CALLED);
        registration.setStatusChangedBy("venue.operator");
        when(registrationService.updateStatus(
                12, REFERENCE, CandidateRegistrationStatus.CALLED, "venue.operator"))
                .thenReturn(registration);

        mockMvc.perform(patch("/api/hiring-drives/12/registrations/"
                        + REFERENCE + "/status")
                        .with(jwt().jwt(token -> token.subject("venue.operator"))
                                .authorities(() -> "ROLE_RECRUITER"))
                        .contentType("application/json")
                        .content("{\"status\":\"CALLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CALLED"))
                .andExpect(jsonPath("$.statusChangedBy").value("venue.operator"));

        verify(registrationService).updateStatus(
                12, REFERENCE, CandidateRegistrationStatus.CALLED, "venue.operator");
    }

    @Test
    void queueSummaryReturnsAllLifecycleCounts() throws Exception {
        EnumMap<CandidateRegistrationStatus, Long> counts =
                new EnumMap<>(CandidateRegistrationStatus.class);
        counts.put(CandidateRegistrationStatus.WAITING, 4L);
        counts.put(CandidateRegistrationStatus.CALLED, 2L);
        counts.put(CandidateRegistrationStatus.COMPLETED, 7L);
        counts.put(CandidateRegistrationStatus.WITHDRAWN, 1L);
        when(registrationService.getStatusCounts(12)).thenReturn(counts);

        mockMvc.perform(get("/api/hiring-drives/12/registrations/summary")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting").value(4))
                .andExpect(jsonPath("$.called").value(2))
                .andExpect(jsonPath("$.completed").value(7))
                .andExpect(jsonPath("$.withdrawn").value(1))
                .andExpect(jsonPath("$.total").value(14));
    }

    @Test
    void authenticatedOperatorCanDownloadPdfResume() throws Exception {
        CandidateRegistration registration = registration();
        when(registrationService.getRegistration(12, REFERENCE)).thenReturn(registration);

        mockMvc.perform(get("/api/hiring-drives/12/registrations/"
                        + REFERENCE + "/resume")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"candidate-" + REFERENCE + ".pdf\""))
                .andExpect(content().bytes("%PDF-resume".getBytes()));
    }

    @Test
    void missingResumeReturnsNotFound() throws Exception {
        CandidateRegistration registration = registration();
        registration.setResume(null);
        when(registrationService.getRegistration(12, REFERENCE)).thenReturn(registration);

        mockMvc.perform(get("/api/hiring-drives/12/registrations/"
                        + REFERENCE + "/resume")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Candidate resume not found"));
    }

    @Test
    void concurrentStatusChangeReturnsConflict() throws Exception {
        when(registrationService.updateStatus(
                eq(12), eq(REFERENCE), eq(CandidateRegistrationStatus.CALLED), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(
                        CandidateRegistration.class, REFERENCE));

        mockMvc.perform(patch("/api/hiring-drives/12/registrations/"
                        + REFERENCE + "/status")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType("application/json")
                        .content("{\"status\":\"CALLED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "The candidate was updated by another operator; reload and try again"));
    }

    @Test
    void invalidStatusRequestIsRejected() throws Exception {
        mockMvc.perform(patch("/api/hiring-drives/12/registrations/"
                        + REFERENCE + "/status")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    private CandidateRegistration registration() {
        CandidateRegistration registration = new CandidateRegistration();
        registration.setRegistrationReference(REFERENCE);
        registration.setFirstName("Asha");
        registration.setLastName("Sharma");
        registration.setEmail("asha@example.com");
        registration.setContactNumber("9876543210");
        registration.setResume("%PDF-resume".getBytes());
        registration.setNotificationDestination("private-alerts@example.com");
        registration.setStatus(CandidateRegistrationStatus.WAITING);
        registration.setRegisteredAt(OffsetDateTime.parse("2026-09-05T12:00:00Z"));
        registration.setStatusChangedAt(OffsetDateTime.parse("2026-09-05T12:00:00Z"));
        return registration;
    }
}
