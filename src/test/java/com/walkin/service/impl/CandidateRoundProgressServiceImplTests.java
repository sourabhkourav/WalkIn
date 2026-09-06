package com.walkin.service.impl;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRoundProgress;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.RoundProgressStatus;
import com.walkin.exception.ResourceConflictException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.repository.CandidateRoundProgressRepository;
import com.walkin.repository.HiringDriveRoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateRoundProgressServiceImplTests {

    private static final Instant NOW = Instant.parse("2026-09-06T10:00:00Z");
    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);

    @Mock
    private CandidateRoundProgressRepository progressRepository;

    @Mock
    private CandidateRegistrationRepository registrationRepository;

    @Mock
    private HiringDriveRoundRepository driveRoundRepository;

    private CandidateRoundProgressServiceImpl progressService;

    @BeforeEach
    void setUp() {
        progressService = new CandidateRoundProgressServiceImpl(
                progressRepository,
                registrationRepository,
                driveRoundRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void enqueuesCandidateInWaitingStateForRoundInSameDrive() {
        CandidateRegistration registration = registrationForDrive(10);
        HiringDriveRound driveRound = roundForDrive(10);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(registration));
        when(driveRoundRepository.findById(2)).thenReturn(Optional.of(driveRound));
        when(progressRepository.save(any(CandidateRoundProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateRoundProgress result = progressService.enqueue(1, 2, " recruiter-one ");

        assertThat(result.getRegistration()).isSameAs(registration);
        assertThat(result.getDriveRound()).isSameAs(driveRound);
        assertThat(result.getStatus()).isEqualTo(RoundProgressStatus.WAITING);
        assertThat(result.getQueuedAt()).isEqualTo(EVENT_TIME);
        assertThat(result.getStatusChangedAt()).isEqualTo(EVENT_TIME);
        assertThat(result.getStatusChangedBy()).isEqualTo("recruiter-one");
    }

    @Test
    void rejectsDuplicateRoundEntryBeforeLoadingRelationships() {
        when(progressRepository.existsByRegistration_RegistrationIdAndDriveRound_DriveRoundId(1, 2))
                .thenReturn(true);

        assertThatThrownBy(() -> progressService.enqueue(1, 2, "system"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Candidate is already queued for this round");

        verifyNoInteractions(registrationRepository, driveRoundRepository);
        verify(progressRepository, never()).save(any());
    }

    @Test
    void rejectsRoundFromDifferentHiringDrive() {
        CandidateRegistration registration = registrationForDrive(10);
        HiringDriveRound driveRound = roundForDrive(11);
        when(registrationRepository.findById(1)).thenReturn(Optional.of(registration));
        when(driveRoundRepository.findById(2)).thenReturn(Optional.of(driveRound));

        assertThatThrownBy(() -> progressService.enqueue(1, 2, "system"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Candidate registration and round belong to different hiring drives");

        verify(progressRepository, never()).save(any());
    }

    @Test
    void recordsCallInterviewAndSelectedResultEvents() {
        CandidateRoundProgress progress = waitingProgress();
        OffsetDateTime reportingTime = EVENT_TIME.plusMinutes(20);
        when(progressRepository.findById(7)).thenReturn(Optional.of(progress));
        when(progressRepository.save(progress)).thenReturn(progress);

        progressService.updateStatus(
                7, RoundProgressStatus.CALLED, reportingTime, "recruiter-one");
        assertThat(progress.getStatus()).isEqualTo(RoundProgressStatus.CALLED);
        assertThat(progress.getReportingTime()).isEqualTo(reportingTime);
        assertThat(progress.getCalledAt()).isEqualTo(EVENT_TIME);

        progressService.updateStatus(
                7, RoundProgressStatus.INTERVIEWING, null, "recruiter-two");
        assertThat(progress.getInterviewStartedAt()).isEqualTo(EVENT_TIME);

        progressService.updateStatus(
                7, RoundProgressStatus.AWAITING_RESULT, null, "recruiter-two");
        assertThat(progress.getInterviewCompletedAt()).isEqualTo(EVENT_TIME);

        progressService.updateStatus(
                7, RoundProgressStatus.SELECTED, null, "recruiter-two");
        assertThat(progress.getResultDecidedAt()).isEqualTo(EVENT_TIME);
        assertThat(progress.getResultDecidedBy()).isEqualTo("recruiter-two");
        assertThat(progress.getStatusChangedBy()).isEqualTo("recruiter-two");
    }

    @Test
    void requiresFutureReportingTimeWhenCandidateIsCalled() {
        CandidateRoundProgress progress = waitingProgress();
        when(progressRepository.findById(7)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> progressService.updateStatus(
                7, RoundProgressStatus.CALLED, EVENT_TIME, "recruiter-one"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reportingTime must be in the future when calling a candidate");

        assertThat(progress.getStatus()).isEqualTo(RoundProgressStatus.WAITING);
        verify(progressRepository, never()).save(any());
    }

    @Test
    void preventsSkippingRequiredRoundStates() {
        CandidateRoundProgress progress = waitingProgress();
        when(progressRepository.findById(7)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> progressService.updateStatus(
                7, RoundProgressStatus.SELECTED, null, "recruiter-one"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Round status cannot change from WAITING to SELECTED");

        verify(progressRepository, never()).save(any());
    }

    @Test
    void makesFinalRoundStatesTerminal() {
        CandidateRoundProgress progress = waitingProgress();
        progress.setStatus(RoundProgressStatus.REJECTED);
        when(progressRepository.findById(7)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> progressService.updateStatus(
                7, RoundProgressStatus.WAITING, null, "recruiter-one"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Round status cannot change from REJECTED to WAITING");

        verify(progressRepository, never()).save(any());
    }

    @Test
    void requiresAuditableActorForEveryMutation() {
        assertThatThrownBy(() -> progressService.enqueue(1, 2, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("changedBy is required");

        verifyNoInteractions(progressRepository, registrationRepository, driveRoundRepository);
    }

    private CandidateRoundProgress waitingProgress() {
        CandidateRoundProgress progress = new CandidateRoundProgress();
        progress.setStatus(RoundProgressStatus.WAITING);
        progress.setQueuedAt(EVENT_TIME);
        progress.setStatusChangedAt(EVENT_TIME);
        progress.setStatusChangedBy("system");
        return progress;
    }

    private CandidateRegistration registrationForDrive(Integer driveId) {
        CandidateRegistration registration = new CandidateRegistration();
        registration.setHiringDrive(drive(driveId));
        return registration;
    }

    private HiringDriveRound roundForDrive(Integer driveId) {
        HiringDriveRound driveRound = new HiringDriveRound();
        driveRound.setHiringDrive(drive(driveId));
        return driveRound;
    }

    private HiringDrive drive(Integer driveId) {
        HiringDrive drive = org.mockito.Mockito.mock(HiringDrive.class);
        when(drive.getDriveId()).thenReturn(driveId);
        return drive;
    }
}
