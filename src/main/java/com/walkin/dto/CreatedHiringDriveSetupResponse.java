package com.walkin.dto;

import com.walkin.service.HiringDriveSetup;

import java.util.List;

public record CreatedHiringDriveSetupResponse(
        HiringDriveResponse drive,
        HiringDriveRegistrationFormResponse registrationForm,
        List<HiringDriveRoundResponse> rounds,
        String registrationToken) {

    public static CreatedHiringDriveSetupResponse from(HiringDriveSetup setup) {
        return new CreatedHiringDriveSetupResponse(
                HiringDriveResponse.from(setup.hiringDrive()),
                HiringDriveRegistrationFormResponse.from(setup.hiringDrive()),
                setup.rounds().stream().map(HiringDriveRoundResponse::from).toList(),
                setup.registrationToken());
    }
}
