package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRequest;
import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.dto.HiringDriveSetupRequest;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveRoundService;
import com.walkin.service.HiringDriveService;
import com.walkin.service.HiringDriveSetup;
import com.walkin.service.HiringDriveSetupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class HiringDriveSetupServiceImpl implements HiringDriveSetupService {

    private final HiringDriveService driveService;
    private final HiringDriveRoundService roundService;

    public HiringDriveSetupServiceImpl(
            HiringDriveService driveService,
            HiringDriveRoundService roundService) {
        this.driveService = driveService;
        this.roundService = roundService;
    }

    @Override
    @Transactional
    public HiringDriveSetup create(HiringDriveSetupRequest request) {
        if (new HashSet<>(request.companyRoundIds()).size() != request.companyRoundIds().size()) {
            throw new IllegalArgumentException("A round can be selected only once");
        }

        HiringDriveCreation creation = driveService.createDrive(new HiringDriveRequest(
                request.companyId(), request.driveName(), request.venue(),
                request.startsAt(), request.endsAt()));
        HiringDrive drive = driveService.updateRegistrationForm(
                creation.hiringDrive().getDriveId(), request.registrationForm());

        List<HiringDriveRound> rounds = new ArrayList<>();
        for (int index = 0; index < request.companyRoundIds().size(); index++) {
            rounds.add(roundService.addRound(
                    drive.getDriveId(),
                    new HiringDriveRoundRequest(
                            request.companyRoundIds().get(index), index + 1)));
        }
        if (request.openRegistration()) {
            drive = driveService.updateStatus(drive.getDriveId(), HiringDriveStatus.OPEN);
        }
        return new HiringDriveSetup(drive, List.copyOf(rounds), creation.registrationToken());
    }
}
