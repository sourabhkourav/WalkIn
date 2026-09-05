package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyCustomRoundRepository;
import com.walkin.repository.HiringDriveRepository;
import com.walkin.repository.HiringDriveRoundRepository;
import com.walkin.service.HiringDriveRoundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class HiringDriveRoundServiceImpl implements HiringDriveRoundService {

    private final HiringDriveRoundRepository driveRoundRepository;
    private final HiringDriveRepository driveRepository;
    private final CompanyCustomRoundRepository companyRoundRepository;

    public HiringDriveRoundServiceImpl(
            HiringDriveRoundRepository driveRoundRepository,
            HiringDriveRepository driveRepository,
            CompanyCustomRoundRepository companyRoundRepository) {
        this.driveRoundRepository = driveRoundRepository;
        this.driveRepository = driveRepository;
        this.companyRoundRepository = companyRoundRepository;
    }

    @Override
    @Transactional
    public HiringDriveRound addRound(Integer driveId, HiringDriveRoundRequest request) {
        HiringDrive drive = getDrive(driveId);
        requireEditableDrive(drive);
        CompanyCustomRound companyRound = companyRoundRepository
                .findById(request.companyRoundId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company round not found with ID: " + request.companyRoundId()));
        requireSameCompany(drive, companyRound);

        if (driveRoundRepository
                .existsByHiringDrive_DriveIdAndCompanyRound_CompanyRoundId(
                        driveId, request.companyRoundId())) {
            throw new ResourceConflictException(
                    "This company round is already assigned to the hiring drive");
        }
        if (driveRoundRepository.existsByHiringDrive_DriveIdAndRoundOrder(
                driveId, request.roundOrder())) {
            throw new ResourceConflictException(
                    "Another round already uses order " + request.roundOrder());
        }

        HiringDriveRound driveRound = new HiringDriveRound();
        driveRound.setHiringDrive(drive);
        driveRound.setCompanyRound(companyRound);
        driveRound.setRoundOrder(request.roundOrder());
        return driveRoundRepository.save(driveRound);
    }

    @Override
    public List<HiringDriveRound> getRounds(Integer driveId) {
        getDrive(driveId);
        return driveRoundRepository.findByHiringDrive_DriveIdOrderByRoundOrderAsc(driveId);
    }

    private HiringDrive getDrive(Integer driveId) {
        return driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hiring drive not found with ID: " + driveId));
    }

    private void requireEditableDrive(HiringDrive drive) {
        if (drive.getStatus() != HiringDriveStatus.DRAFT
                && drive.getStatus() != HiringDriveStatus.OPEN) {
            throw new ResourceConflictException(
                    "Rounds can be added only to DRAFT or OPEN hiring drives");
        }
    }

    private void requireSameCompany(HiringDrive drive, CompanyCustomRound companyRound) {
        if (!Objects.equals(
                drive.getCompany().getCompanyId(),
                companyRound.getCompany().getCompanyId())) {
            throw new ResourceConflictException(
                    "The company round belongs to a different company");
        }
    }
}
