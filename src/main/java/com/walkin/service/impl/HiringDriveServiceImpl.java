package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRequest;
import com.walkin.entity.Company;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyRepository;
import com.walkin.repository.HiringDriveRepository;
import com.walkin.security.RegistrationTokenService;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class HiringDriveServiceImpl implements HiringDriveService {

    private static final int MAX_TOKEN_GENERATION_ATTEMPTS = 5;
    private static final Map<HiringDriveStatus, Set<HiringDriveStatus>> ALLOWED_TRANSITIONS = Map.of(
            HiringDriveStatus.DRAFT, Set.of(HiringDriveStatus.OPEN, HiringDriveStatus.CANCELLED),
            HiringDriveStatus.OPEN, Set.of(HiringDriveStatus.CLOSED, HiringDriveStatus.CANCELLED),
            HiringDriveStatus.CLOSED, Set.of(),
            HiringDriveStatus.CANCELLED, Set.of());

    private final HiringDriveRepository driveRepository;
    private final CompanyRepository companyRepository;
    private final RegistrationTokenService tokenService;
    private final Clock clock;

    public HiringDriveServiceImpl(
            HiringDriveRepository driveRepository,
            CompanyRepository companyRepository,
            RegistrationTokenService tokenService,
            Clock clock) {
        this.driveRepository = driveRepository;
        this.companyRepository = companyRepository;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public HiringDriveCreation createDrive(HiringDriveRequest request) {
        validateTimeRange(request.startsAt(), request.endsAt());
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ID: " + request.companyId()));
        IssuedToken issuedToken = issueUniqueToken();

        HiringDrive drive = new HiringDrive();
        drive.setCompany(company);
        drive.setDriveName(request.driveName().trim());
        drive.setVenue(request.venue().trim());
        drive.setStartsAt(request.startsAt());
        drive.setEndsAt(request.endsAt());
        drive.setStatus(HiringDriveStatus.DRAFT);
        drive.setRegistrationTokenHash(issuedToken.hash());
        drive.setTokenExpiresAt(request.endsAt());

        return new HiringDriveCreation(
                driveRepository.save(drive),
                issuedToken.rawToken());
    }

    @Override
    public HiringDrive getDriveById(Integer driveId) {
        return driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hiring drive not found with ID: " + driveId));
    }

    @Override
    public Page<HiringDrive> getDrives(Pageable pageable) {
        return driveRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public HiringDrive updateStatus(Integer driveId, HiringDriveStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        HiringDrive drive = getDriveById(driveId);
        HiringDriveStatus currentStatus = drive.getStatus();
        if (currentStatus == targetStatus) {
            return drive;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new ResourceConflictException(
                    "Hiring drive status cannot change from " + currentStatus + " to " + targetStatus);
        }
        if (targetStatus == HiringDriveStatus.OPEN && !drive.getEndsAt().isAfter(now())) {
            throw new ResourceConflictException("An ended hiring drive cannot be opened");
        }

        drive.setStatus(targetStatus);
        return driveRepository.save(drive);
    }

    @Override
    public HiringDrive getOpenDriveByRegistrationToken(String registrationToken) {
        if (registrationToken == null || registrationToken.isBlank()) {
            throw unavailableDrive();
        }
        String tokenHash = tokenService.hashToken(registrationToken);
        HiringDrive drive = driveRepository.findByRegistrationTokenHash(tokenHash)
                .orElseThrow(this::unavailableDrive);
        if (drive.getStatus() != HiringDriveStatus.OPEN
                || !drive.getTokenExpiresAt().isAfter(now())
                || !drive.getEndsAt().isAfter(now())) {
            throw unavailableDrive();
        }
        return drive;
    }

    private void validateTimeRange(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (startsAt == null || endsAt == null || !startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException("startsAt must be before endsAt");
        }
        if (!endsAt.isAfter(now())) {
            throw new IllegalArgumentException("endsAt must be in the future");
        }
    }

    private IssuedToken issueUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_GENERATION_ATTEMPTS; attempt++) {
            String rawToken = tokenService.generateToken();
            String tokenHash = tokenService.hashToken(rawToken);
            if (!driveRepository.existsByRegistrationTokenHash(tokenHash)) {
                return new IssuedToken(rawToken, tokenHash);
            }
        }
        throw new IllegalStateException("Unable to generate a unique registration token");
    }

    private ResourceNotFoundException unavailableDrive() {
        return new ResourceNotFoundException("Hiring drive is unavailable");
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private record IssuedToken(String rawToken, String hash) {
    }
}
