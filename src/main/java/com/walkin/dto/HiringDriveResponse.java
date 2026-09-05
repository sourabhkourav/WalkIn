package com.walkin.dto;

import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveStatus;

import java.time.OffsetDateTime;

public record HiringDriveResponse(
        Integer driveId,
        Integer companyId,
        String driveName,
        String venue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        HiringDriveStatus status,
        OffsetDateTime tokenExpiresAt) {

    public static HiringDriveResponse from(HiringDrive drive) {
        return new HiringDriveResponse(
                drive.getDriveId(),
                drive.getCompany().getCompanyId(),
                drive.getDriveName(),
                drive.getVenue(),
                drive.getStartsAt(),
                drive.getEndsAt(),
                drive.getStatus(),
                drive.getTokenExpiresAt());
    }
}
