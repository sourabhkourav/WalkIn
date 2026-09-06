package com.walkin.service.impl;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRoundProgress;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.RoundProgressStatus;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.repository.CandidateRoundProgressRepository;
import com.walkin.repository.HiringDriveRoundRepository;
import com.walkin.service.CandidateRoundProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CandidateRoundProgressServiceImpl implements CandidateRoundProgressService {

    private static final Map<RoundProgressStatus, Set<RoundProgressStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    RoundProgressStatus.WAITING,
                    Set.of(RoundProgressStatus.CALLED, RoundProgressStatus.WITHDRAWN),
                    RoundProgressStatus.CALLED,
                    Set.of(
                            RoundProgressStatus.INTERVIEWING,
                            RoundProgressStatus.MISSED,
                            RoundProgressStatus.WITHDRAWN),
                    RoundProgressStatus.INTERVIEWING,
                    Set.of(
                            RoundProgressStatus.AWAITING_RESULT,
                            RoundProgressStatus.WITHDRAWN),
                    RoundProgressStatus.AWAITING_RESULT,
                    Set.of(RoundProgressStatus.SELECTED, RoundProgressStatus.REJECTED),
                    RoundProgressStatus.SELECTED, Set.of(),
                    RoundProgressStatus.REJECTED, Set.of(),
                    RoundProgressStatus.MISSED, Set.of(),
                    RoundProgressStatus.WITHDRAWN, Set.of());

    private final CandidateRoundProgressRepository progressRepository;
    private final CandidateRegistrationRepository registrationRepository;
    private final HiringDriveRoundRepository driveRoundRepository;
    private final Clock clock;

    public CandidateRoundProgressServiceImpl(
            CandidateRoundProgressRepository progressRepository,
            CandidateRegistrationRepository registrationRepository,
            HiringDriveRoundRepository driveRoundRepository,
            Clock clock) {
        this.progressRepository = progressRepository;
        this.registrationRepository = registrationRepository;
        this.driveRoundRepository = driveRoundRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CandidateRoundProgress enqueue(
            Integer registrationId,
            Integer driveRoundId,
            String changedBy) {
        requireActor(changedBy);
        if (progressRepository.existsByRegistration_RegistrationIdAndDriveRound_DriveRoundId(
                registrationId, driveRoundId)) {
            throw new ResourceConflictException("Candidate is already queued for this round");
        }

        CandidateRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate registration not found with ID: " + registrationId));
        HiringDriveRound driveRound = driveRoundRepository.findById(driveRoundId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hiring drive round not found with ID: " + driveRoundId));
        requireSameDrive(registration, driveRound);

        OffsetDateTime eventTime = now();
        CandidateRoundProgress progress = new CandidateRoundProgress();
        progress.setRegistration(registration);
        progress.setDriveRound(driveRound);
        progress.setStatus(RoundProgressStatus.WAITING);
        progress.setQueuedAt(eventTime);
        progress.setStatusChangedAt(eventTime);
        progress.setStatusChangedBy(changedBy.strip());
        return progressRepository.save(progress);
    }

    @Override
    public CandidateRoundProgress getById(Integer progressId) {
        return progressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate round progress not found with ID: " + progressId));
    }

    @Override
    @Transactional
    public CandidateRoundProgress updateStatus(
            Integer progressId,
            RoundProgressStatus targetStatus,
            OffsetDateTime reportingTime,
            String changedBy) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        requireActor(changedBy);
        CandidateRoundProgress progress = getById(progressId);
        RoundProgressStatus currentStatus = progress.getStatus();
        if (currentStatus == targetStatus) {
            return progress;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new ResourceConflictException(
                    "Round status cannot change from " + currentStatus + " to " + targetStatus);
        }

        OffsetDateTime eventTime = now();
        applyStatusDetails(progress, targetStatus, reportingTime, changedBy.strip(), eventTime);
        progress.setStatus(targetStatus);
        progress.setStatusChangedAt(eventTime);
        progress.setStatusChangedBy(changedBy.strip());
        return progressRepository.save(progress);
    }

    private void applyStatusDetails(
            CandidateRoundProgress progress,
            RoundProgressStatus targetStatus,
            OffsetDateTime reportingTime,
            String changedBy,
            OffsetDateTime eventTime) {
        switch (targetStatus) {
            case CALLED -> {
                if (reportingTime == null || !reportingTime.isAfter(eventTime)) {
                    throw new IllegalArgumentException(
                            "reportingTime must be in the future when calling a candidate");
                }
                progress.setReportingTime(reportingTime);
                progress.setCalledAt(eventTime);
            }
            case INTERVIEWING -> progress.setInterviewStartedAt(eventTime);
            case AWAITING_RESULT -> progress.setInterviewCompletedAt(eventTime);
            case SELECTED, REJECTED -> {
                progress.setResultDecidedAt(eventTime);
                progress.setResultDecidedBy(changedBy);
            }
            default -> {
                // These states do not add a dedicated event timestamp.
            }
        }
    }

    private void requireSameDrive(
            CandidateRegistration registration,
            HiringDriveRound driveRound) {
        Integer registrationDriveId = registration.getHiringDrive().getDriveId();
        Integer roundDriveId = driveRound.getHiringDrive().getDriveId();
        if (!Objects.equals(registrationDriveId, roundDriveId)) {
            throw new ResourceConflictException(
                    "Candidate registration and round belong to different hiring drives");
        }
    }

    private void requireActor(String changedBy) {
        if (changedBy == null || changedBy.isBlank()) {
            throw new IllegalArgumentException("changedBy is required");
        }
        if (changedBy.strip().length() > 100) {
            throw new IllegalArgumentException("changedBy must not exceed 100 characters");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
