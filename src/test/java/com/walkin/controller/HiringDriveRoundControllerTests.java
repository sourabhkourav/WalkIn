package com.walkin.controller;

import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.InterviewRound;
import com.walkin.service.HiringDriveRoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class HiringDriveRoundControllerTests {

    @Autowired
    private WebApplicationContext applicationContext;

    @MockitoBean
    private HiringDriveRoundService driveRoundService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/hiring-drives/10/rounds"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruiterCanListOrderedDriveRounds() throws Exception {
        HiringDriveRound driveRound = driveRound();
        when(driveRoundService.getRounds(10)).thenReturn(List.of(driveRound));

        mockMvc.perform(get("/api/hiring-drives/10/rounds")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driveRoundId").value(30))
                .andExpect(jsonPath("$[0].driveId").value(10))
                .andExpect(jsonPath("$[0].companyRoundId").value(20))
                .andExpect(jsonPath("$[0].interviewRoundId").value(5))
                .andExpect(jsonPath("$[0].roundName").value("Technical interview"))
                .andExpect(jsonPath("$[0].roundOrder").value(1));
    }

    @Test
    void recruiterCannotAddRound() throws Exception {
        mockMvc.perform(post("/api/hiring-drives/10/rounds")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorAddsRound() throws Exception {
        HiringDriveRound driveRound = driveRound();
        when(driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .thenReturn(driveRound);

        mockMvc.perform(post("/api/hiring-drives/10/rounds")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/hiring-drives/10/rounds/30"))
                .andExpect(jsonPath("$.roundName").value("Technical interview"))
                .andExpect(jsonPath("$.roundOrder").value(1));

        verify(driveRoundService).addRound(
                10, new HiringDriveRoundRequest(20, 1));
    }

    @Test
    void invalidRoundAssignmentIsRejected() throws Exception {
        mockMvc.perform(post("/api/hiring-drives/10/rounds")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyRoundId\":0,\"roundOrder\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.companyRoundId").exists())
                .andExpect(jsonPath("$.fieldErrors.roundOrder").exists());
    }

    private HiringDriveRound driveRound() {
        HiringDriveRound driveRound = org.mockito.Mockito.mock(HiringDriveRound.class);
        HiringDrive drive = org.mockito.Mockito.mock(HiringDrive.class);
        CompanyCustomRound companyRound = org.mockito.Mockito.mock(CompanyCustomRound.class);
        InterviewRound interviewRound = org.mockito.Mockito.mock(InterviewRound.class);
        when(driveRound.getDriveRoundId()).thenReturn(30);
        when(driveRound.getHiringDrive()).thenReturn(drive);
        when(drive.getDriveId()).thenReturn(10);
        when(driveRound.getCompanyRound()).thenReturn(companyRound);
        when(companyRound.getCompanyRoundId()).thenReturn(20);
        when(companyRound.getInterviewRound()).thenReturn(interviewRound);
        when(interviewRound.getRoundId()).thenReturn(5);
        when(interviewRound.getRoundName()).thenReturn("Technical interview");
        when(driveRound.getRoundOrder()).thenReturn(1);
        return driveRound;
    }

    private String validRequest() {
        return "{\"companyRoundId\":20,\"roundOrder\":1}";
    }
}
