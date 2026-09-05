package com.walkin.dto;

import com.walkin.service.HiringDriveCreation;

public record CreatedHiringDriveResponse(
        HiringDriveResponse drive,
        String registrationToken) {

    public static CreatedHiringDriveResponse from(HiringDriveCreation creation) {
        return new CreatedHiringDriveResponse(
                HiringDriveResponse.from(creation.hiringDrive()),
                creation.registrationToken());
    }
}
