package com.walkin.service.impl;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.NotificationChannel;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.exception.ResourceConflictException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.service.HiringDriveService;
import com.walkin.service.ResumeUploadValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateRegistrationServiceImplTests {

    private static final Instant NOW = Instant.parse("2026-09-05T13:00:00Z");

    @Mock
    private CandidateRegistrationRepository registrationRepository;

    @Mock
    private HiringDriveService driveService;

    @Mock
    private ResumeUploadValidator resumeValidator;

    private HiringDrive drive;

    private CandidateRegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        drive = new HiringDrive();
        registrationService = new CandidateRegistrationServiceImpl(
                registrationRepository,
                driveService,
                resumeValidator,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void registersCandidateWithNormalizedDetailsAndSeparateNotificationDestination() {
        CandidateRegistrationRequest request = validRequest();
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", "%PDF-test".getBytes());
        request.setResume(resume);
        stubDriveConfiguration(RegistrationFieldRequirement.OPTIONAL);
        when(resumeValidator.validate(resume, RegistrationFieldRequirement.OPTIONAL))
                .thenReturn("stored-pdf".getBytes());
        when(registrationRepository.save(any(CandidateRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateRegistration registration = registrationService.register("drive-token", request);

        assertThat(registration.getRegistrationReference()).isNotNull();
        assertThat(registration.getHiringDrive()).isSameAs(drive);
        assertThat(registration.getFirstName()).isEqualTo("Asha");
        assertThat(registration.getLastName()).isEqualTo("Sharma");
        assertThat(registration.getEmail()).isEqualTo("asha@example.com");
        assertThat(registration.getContactNumber()).isNull();
        assertThat(registration.getNotificationChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(registration.getNotificationDestination())
                .isEqualTo("personal-alerts@example.com");
        assertThat(registration.getAdvanceNoticeMinutes()).isEqualTo(30);
        assertThat(registration.getStatus()).isEqualTo(CandidateRegistrationStatus.WAITING);
        assertThat(registration.getRegisteredAt().toInstant()).isEqualTo(NOW);
        assertThat(registration.getResume()).isEqualTo("stored-pdf".getBytes());
    }

    @Test
    void rejectsMissingRequiredField() {
        CandidateRegistrationRequest request = validRequest();
        request.setFirstName(" ");
        stubDriveConfiguration(RegistrationFieldRequirement.HIDDEN);

        assertThatThrownBy(() -> registrationService.register("drive-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("firstName is required");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void rejectsDataForHiddenCompanyField() {
        CandidateRegistrationRequest request = validRequest();
        request.setContactNumber("9876543210");
        stubDriveConfiguration(RegistrationFieldRequirement.HIDDEN);

        assertThatThrownBy(() -> registrationService.register("drive-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("contactNumber is not accepted for this hiring drive");
    }

    @Test
    void validatesDestinationAgainstSelectedNotificationChannel() {
        CandidateRegistrationRequest request = validRequest();
        request.setNotificationChannel(NotificationChannel.SMS);
        request.setNotificationDestination("not-a-phone-number");
        stubDriveConfiguration(RegistrationFieldRequirement.HIDDEN);

        assertThatThrownBy(() -> registrationService.register("drive-token", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "notificationDestination must contain 10 to 15 digits with an optional leading +");
    }

    @Test
    void rejectsDuplicateCandidateWithinSameDrive() {
        CandidateRegistrationRequest request = validRequest();
        stubDriveConfiguration(RegistrationFieldRequirement.HIDDEN);
        when(registrationRepository
                .existsByHiringDrive_DriveIdAndEmailIgnoreCase(null, "Asha@Example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> registrationService.register("drive-token", request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage(
                        "A candidate with this email is already registered for the hiring drive");

        verify(registrationRepository, never()).save(any());
    }

    private void stubDriveConfiguration(RegistrationFieldRequirement resumeRequirement) {
        when(driveService.getOpenDriveByRegistrationToken("drive-token")).thenReturn(drive);
        drive.setFirstNameRequirement(RegistrationFieldRequirement.REQUIRED);
        drive.setLastNameRequirement(RegistrationFieldRequirement.OPTIONAL);
        drive.setEmailRequirement(RegistrationFieldRequirement.REQUIRED);
        drive.setContactNumberRequirement(RegistrationFieldRequirement.HIDDEN);
        drive.setResumeRequirement(resumeRequirement);
    }

    private CandidateRegistrationRequest validRequest() {
        CandidateRegistrationRequest request = new CandidateRegistrationRequest();
        request.setFirstName("  Asha  ");
        request.setLastName(" Sharma ");
        request.setEmail("Asha@Example.com");
        request.setNotificationChannel(NotificationChannel.EMAIL);
        request.setNotificationDestination(" Personal-Alerts@Example.com ");
        request.setAdvanceNoticeMinutes(30);
        return request;
    }
}
