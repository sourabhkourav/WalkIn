package com.walkin.dto;

import com.walkin.entity.HiringDrive;

import java.time.OffsetDateTime;

public record PublicHiringDriveResponse(
        String companyName,
        String driveName,
        String venue,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        HiringDriveRegistrationFormResponse registrationForm) {

    public static PublicHiringDriveResponse from(HiringDrive drive) {
        return new PublicHiringDriveResponse(
                drive.getCompany().getCompanyName(),
                drive.getDriveName(),
                drive.getVenue(),
                drive.getStartsAt(),
                drive.getEndsAt(),
                HiringDriveRegistrationFormResponse.from(drive));
    }
}
