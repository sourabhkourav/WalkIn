package com.walkin.service;

import com.walkin.dto.HiringDriveRequest;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HiringDriveService {

    HiringDriveCreation createDrive(HiringDriveRequest request);

    HiringDrive getDriveById(Integer driveId);

    Page<HiringDrive> getDrives(Pageable pageable);

    HiringDrive updateStatus(Integer driveId, HiringDriveStatus status);

    HiringDrive getOpenDriveByRegistrationToken(String registrationToken);
}
