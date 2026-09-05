package com.walkin.service;

import com.walkin.entity.HiringDrive;

public record HiringDriveCreation(
        HiringDrive hiringDrive,
        String registrationToken) {
}
