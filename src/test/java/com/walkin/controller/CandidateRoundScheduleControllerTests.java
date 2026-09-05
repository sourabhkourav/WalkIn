package com.walkin.controller;

import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.ScheduleStatus;
import com.walkin.entity.Student;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.service.CandidateRoundScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class CandidateRoundScheduleControllerTests {

    @Autowired
    private WebApplicationContext applicationContext;

    @MockitoBean
    private CandidateRoundScheduleService scheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/candidate-round-schedules"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruiterCanReadSchedulesWithoutCandidateDetails() throws Exception {
        CandidateRoundSchedule schedule = schedule();
        when(scheduleService.getSchedules(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(schedule)));

        mockMvc.perform(get("/api/candidate-round-schedules")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].scheduleId").value(10))
                .andExpect(jsonPath("$.content[0].studentId").value(1))
                .andExpect(jsonPath("$.content[0].companyRoundId").value(2))
                .andExpect(jsonPath("$.content[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.content[0].student").doesNotExist())
                .andExpect(jsonPath("$.content[0].companyRound").doesNotExist());
    }

    @Test
    void recruiterCannotCreateSchedule() throws Exception {
        mockMvc.perform(post("/api/candidate-round-schedules")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanCreateSchedule() throws Exception {
        CandidateRoundSchedule schedule = schedule();
        when(scheduleService.createSchedule(any())).thenReturn(schedule);

        mockMvc.perform(post("/api/candidate-round-schedules")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/candidate-round-schedules/10"))
                .andExpect(jsonPath("$.scheduleId").value(10))
                .andExpect(jsonPath("$.reportingTime").value("2099-01-01T10:00:00Z"));

        verify(scheduleService).createSchedule(any());
    }

    @Test
    void invalidScheduleRequestIsRejectedBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/candidate-round-schedules")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 0,
                                  "companyRoundId": -1,
                                  "reportingTime": "2020-01-01T10:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.studentId").exists())
                .andExpect(jsonPath("$.fieldErrors.companyRoundId").exists())
                .andExpect(jsonPath("$.fieldErrors.reportingTime").exists());
    }

    @Test
    void missingScheduleReturnsNotFound() throws Exception {
        when(scheduleService.getScheduleById(999))
                .thenThrow(new ResourceNotFoundException(
                        "Candidate round schedule not found with ID: 999"));

        mockMvc.perform(get("/api/candidate-round-schedules/999")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Candidate round schedule not found with ID: 999"));
    }

    @Test
    void invalidPaginationIsRejected() throws Exception {
        mockMvc.perform(get("/api/candidate-round-schedules?size=101")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must be between 1 and 100"));
    }

    @Test
    void administratorCanRescheduleFutureReportingTime() throws Exception {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.SCHEDULED);
        when(scheduleService.reschedule(eq(10), any(OffsetDateTime.class))).thenReturn(schedule);

        mockMvc.perform(put("/api/candidate-round-schedules/10/reporting-time")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportingTime\":\"2099-01-01T10:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportingTime").value("2099-01-01T10:00:00Z"));
    }

    @Test
    void pastRescheduleRequestIsRejected() throws Exception {
        mockMvc.perform(put("/api/candidate-round-schedules/10/reporting-time")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportingTime\":\"2020-01-01T10:00:00Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reportingTime").exists());
    }

    @Test
    void administratorCanAdvanceScheduleStatus() throws Exception {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.NOTIFIED);
        when(scheduleService.updateStatus(10, ScheduleStatus.NOTIFIED)).thenReturn(schedule);

        mockMvc.perform(patch("/api/candidate-round-schedules/10/status")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOTIFIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOTIFIED"));
    }

    @Test
    void recruiterCannotChangeScheduleStatus() throws Exception {
        mockMvc.perform(patch("/api/candidate-round-schedules/10/status")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOTIFIED\"}"))
                .andExpect(status().isForbidden());
    }

    private CandidateRoundSchedule schedule() {
        return schedule(ScheduleStatus.SCHEDULED);
    }

    private CandidateRoundSchedule schedule(ScheduleStatus status) {
        CandidateRoundSchedule schedule = org.mockito.Mockito.mock(CandidateRoundSchedule.class);
        Student student = org.mockito.Mockito.mock(Student.class);
        CompanyCustomRound companyRound = org.mockito.Mockito.mock(CompanyCustomRound.class);
        when(schedule.getScheduleId()).thenReturn(10);
        when(schedule.getStudent()).thenReturn(student);
        when(student.getStudentId()).thenReturn(1);
        when(schedule.getCompanyRound()).thenReturn(companyRound);
        when(companyRound.getCompanyRoundId()).thenReturn(2);
        when(schedule.getReportingTime()).thenReturn(
                OffsetDateTime.parse("2099-01-01T10:00:00Z"));
        when(schedule.getStatus()).thenReturn(status);
        return schedule;
    }

    private String validRequest() {
        return """
                {
                  "studentId": 1,
                  "companyRoundId": 2,
                  "reportingTime": "2099-01-01T10:00:00Z"
                }
                """;
    }
}
