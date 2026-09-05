package com.walkin.dto;

import com.walkin.entity.HiringDrive;
import com.walkin.entity.RegistrationFieldRequirement;

public record HiringDriveRegistrationFormResponse(
        RegistrationFieldRequirement firstName,
        RegistrationFieldRequirement lastName,
        RegistrationFieldRequirement email,
        RegistrationFieldRequirement contactNumber,
        RegistrationFieldRequirement resume) {

    public static HiringDriveRegistrationFormResponse from(HiringDrive drive) {
        return new HiringDriveRegistrationFormResponse(
                drive.getFirstNameRequirement(),
                drive.getLastNameRequirement(),
                drive.getEmailRequirement(),
                drive.getContactNumberRequirement(),
                drive.getResumeRequirement());
    }
}
