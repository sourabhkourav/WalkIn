package com.walkin.service.impl;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.NotificationChannel;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.service.HiringDriveService;
import com.walkin.service.ResumeUploadValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        assertThat(registration.getStatusChangedAt().toInstant()).isEqualTo(NOW);
        assertThat(registration.getStatusChangedBy()).isNull();
        assertThat(registration.getResume()).isEqualTo("stored-pdf".getBytes());
    }

    @Test
    void listsDriveRegistrationsWithQueueFilters() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<CandidateRegistration>>any(),
                org.mockito.ArgumentMatchers.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(new CandidateRegistration()), pageable, 1));

        var result = registrationService.getRegistrations(
                12, CandidateRegistrationStatus.WAITING, "  Asha  ", pageable);

        assertThat(result.getTotalElements()).isOne();
        verify(driveService).getDriveById(12);
        verify(registrationRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<CandidateRegistration>>any(),
                org.mockito.ArgumentMatchers.eq(pageable));
    }

    @Test
    void registrationLookupIsScopedToDrive() {
        UUID reference = UUID.randomUUID();
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findByHiringDrive_DriveIdAndRegistrationReference(
                12, reference)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getRegistration(12, reference))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Candidate registration not found");
    }

    @Test
    void rejectsOverlongQueueSearchBeforeQueryingDatabase() {
        when(driveService.getDriveById(12)).thenReturn(drive);

        assertThatThrownBy(() -> registrationService.getRegistrations(
                12, null, "a".repeat(101), PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query must be 100 characters or fewer");

        verifyNoInteractions(registrationRepository);
    }

    @Test
    void movesWaitingCandidateToCalledAndRecordsOperator() {
        UUID reference = UUID.randomUUID();
        CandidateRegistration registration = registration(
                reference, CandidateRegistrationStatus.WAITING);
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findByHiringDrive_DriveIdAndRegistrationReference(
                12, reference)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(registration)).thenReturn(registration);

        CandidateRegistration result = registrationService.updateStatus(
                12, reference, CandidateRegistrationStatus.CALLED, "venue.operator");

        assertThat(result.getStatus()).isEqualTo(CandidateRegistrationStatus.CALLED);
        assertThat(result.getStatusChangedAt().toInstant()).isEqualTo(NOW);
        assertThat(result.getStatusChangedBy()).isEqualTo("venue.operator");
        verify(registrationRepository).save(registration);
    }

    @Test
    void permitsCalledCandidateToReturnToWaiting() {
        UUID reference = UUID.randomUUID();
        CandidateRegistration registration = registration(
                reference, CandidateRegistrationStatus.CALLED);
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findByHiringDrive_DriveIdAndRegistrationReference(
                12, reference)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(registration)).thenReturn(registration);

        CandidateRegistration result = registrationService.updateStatus(
                12, reference, CandidateRegistrationStatus.WAITING, "venue.operator");

        assertThat(result.getStatus()).isEqualTo(CandidateRegistrationStatus.WAITING);
    }

    @Test
    void rejectsTransitionOutOfTerminalStatus() {
        UUID reference = UUID.randomUUID();
        CandidateRegistration registration = registration(
                reference, CandidateRegistrationStatus.COMPLETED);
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findByHiringDrive_DriveIdAndRegistrationReference(
                12, reference)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.updateStatus(
                12, reference, CandidateRegistrationStatus.WAITING, "venue.operator"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Candidate status cannot change from COMPLETED to WAITING");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void repeatingCurrentStatusIsIdempotent() {
        UUID reference = UUID.randomUUID();
        CandidateRegistration registration = registration(
                reference, CandidateRegistrationStatus.WAITING);
        OffsetDateTime originalChangeTime = registration.getStatusChangedAt();
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.findByHiringDrive_DriveIdAndRegistrationReference(
                12, reference)).thenReturn(Optional.of(registration));

        CandidateRegistration result = registrationService.updateStatus(
                12, reference, CandidateRegistrationStatus.WAITING, "venue.operator");

        assertThat(result.getStatusChangedAt()).isEqualTo(originalChangeTime);
        assertThat(result.getStatusChangedBy()).isNull();
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void countsEveryQueueStatusForDriveSummary() {
        when(driveService.getDriveById(12)).thenReturn(drive);
        when(registrationRepository.countByHiringDrive_DriveIdAndStatus(
                12, CandidateRegistrationStatus.WAITING)).thenReturn(4L);
        when(registrationRepository.countByHiringDrive_DriveIdAndStatus(
                12, CandidateRegistrationStatus.CALLED)).thenReturn(2L);
        when(registrationRepository.countByHiringDrive_DriveIdAndStatus(
                12, CandidateRegistrationStatus.COMPLETED)).thenReturn(7L);
        when(registrationRepository.countByHiringDrive_DriveIdAndStatus(
                12, CandidateRegistrationStatus.WITHDRAWN)).thenReturn(1L);

        var counts = registrationService.getStatusCounts(12);

        assertThat(counts).containsEntry(CandidateRegistrationStatus.WAITING, 4L)
                .containsEntry(CandidateRegistrationStatus.CALLED, 2L)
                .containsEntry(CandidateRegistrationStatus.COMPLETED, 7L)
                .containsEntry(CandidateRegistrationStatus.WITHDRAWN, 1L);
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

    private CandidateRegistration registration(
            UUID reference,
            CandidateRegistrationStatus status) {
        CandidateRegistration registration = new CandidateRegistration();
        registration.setRegistrationReference(reference);
        registration.setHiringDrive(drive);
        registration.setStatus(status);
        registration.setRegisteredAt(OffsetDateTime.parse("2026-09-05T12:00:00Z"));
        registration.setStatusChangedAt(OffsetDateTime.parse("2026-09-05T12:00:00Z"));
        return registration;
    }
}
