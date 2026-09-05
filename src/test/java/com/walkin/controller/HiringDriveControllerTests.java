package com.walkin.controller;

import com.walkin.entity.Company;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class HiringDriveControllerTests {

    @Autowired
    private WebApplicationContext applicationContext;

    @MockitoBean
    private HiringDriveService driveService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void managementEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/hiring-drives"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recruiterCanReadDriveWithoutTokenMaterial() throws Exception {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT);
        when(driveService.getDrives(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(drive)));

        mockMvc.perform(get("/api/hiring-drives")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].driveId").value(10))
                .andExpect(jsonPath("$.content[0].companyId").value(1))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.content[0].registrationToken").doesNotExist())
                .andExpect(jsonPath("$.content[0].registrationTokenHash").doesNotExist());
    }

    @Test
    void recruiterCannotCreateDrive() throws Exception {
        mockMvc.perform(post("/api/hiring-drives")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCreatesDriveAndReceivesTokenOnce() throws Exception {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT);
        when(driveService.createDrive(any()))
                .thenReturn(new HiringDriveCreation(drive, "one-time-registration-token"));

        mockMvc.perform(post("/api/hiring-drives")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/hiring-drives/10"))
                .andExpect(jsonPath("$.drive.driveId").value(10))
                .andExpect(jsonPath("$.drive.status").value("DRAFT"))
                .andExpect(jsonPath("$.registrationToken")
                        .value("one-time-registration-token"))
                .andExpect(jsonPath("$.registrationTokenHash").doesNotExist());
    }

    @Test
    void invalidDriveRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/hiring-drives")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": 0,
                                  "driveName": "",
                                  "venue": "",
                                  "startsAt": "2020-01-01T10:00:00Z",
                                  "endsAt": "2020-01-01T11:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.companyId").exists())
                .andExpect(jsonPath("$.fieldErrors.driveName").exists())
                .andExpect(jsonPath("$.fieldErrors.venue").exists())
                .andExpect(jsonPath("$.fieldErrors.startsAt").exists())
                .andExpect(jsonPath("$.fieldErrors.endsAt").exists());
    }

    @Test
    void administratorCanOpenDrive() throws Exception {
        HiringDrive drive = drive(HiringDriveStatus.OPEN);
        when(driveService.updateStatus(10, HiringDriveStatus.OPEN)).thenReturn(drive);

        mockMvc.perform(patch("/api/hiring-drives/10/status")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void recruiterCannotChangeDriveStatus() throws Exception {
        mockMvc.perform(patch("/api/hiring-drives/10/status")
                        .with(jwt().authorities(() -> "ROLE_RECRUITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicLookupRequiresNoAuthenticationAndExposesMinimalDetails() throws Exception {
        HiringDrive drive = drive(HiringDriveStatus.OPEN);
        when(driveService.getOpenDriveByRegistrationToken("valid-token")).thenReturn(drive);

        mockMvc.perform(get("/api/public/hiring-drives/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Acme"))
                .andExpect(jsonPath("$.driveName").value("Engineering Drive"))
                .andExpect(jsonPath("$.venue").value("Convention Centre"))
                .andExpect(jsonPath("$.driveId").doesNotExist())
                .andExpect(jsonPath("$.companyId").doesNotExist())
                .andExpect(jsonPath("$.registrationToken").doesNotExist())
                .andExpect(jsonPath("$.tokenExpiresAt").doesNotExist());
    }

    @Test
    void unavailablePublicDriveReturnsGenericNotFoundResponse() throws Exception {
        when(driveService.getOpenDriveByRegistrationToken("invalid-token"))
                .thenThrow(new ResourceNotFoundException("Hiring drive is unavailable"));

        mockMvc.perform(get("/api/public/hiring-drives/invalid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hiring drive is unavailable"));
    }

    @Test
    void invalidPaginationIsRejected() throws Exception {
        mockMvc.perform(get("/api/hiring-drives?size=101")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must be between 1 and 100"));
    }

    private HiringDrive drive(HiringDriveStatus status) {
        HiringDrive drive = org.mockito.Mockito.mock(HiringDrive.class);
        Company company = org.mockito.Mockito.mock(Company.class);
        when(drive.getDriveId()).thenReturn(10);
        when(drive.getCompany()).thenReturn(company);
        when(company.getCompanyId()).thenReturn(1);
        when(company.getCompanyName()).thenReturn("Acme");
        when(drive.getDriveName()).thenReturn("Engineering Drive");
        when(drive.getVenue()).thenReturn("Convention Centre");
        when(drive.getStartsAt()).thenReturn(OffsetDateTime.parse("2099-01-01T09:00:00Z"));
        when(drive.getEndsAt()).thenReturn(OffsetDateTime.parse("2099-01-01T17:00:00Z"));
        when(drive.getStatus()).thenReturn(status);
        when(drive.getTokenExpiresAt()).thenReturn(OffsetDateTime.parse("2099-01-01T17:00:00Z"));
        return drive;
    }

    private String validRequest() {
        return """
                {
                  "companyId": 1,
                  "driveName": "Engineering Drive",
                  "venue": "Convention Centre",
                  "startsAt": "2099-01-01T09:00:00Z",
                  "endsAt": "2099-01-01T17:00:00Z"
                }
                """;
    }
}
