package com.walkin.service;

import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;

import java.util.List;

public record HiringDriveSetup(
        HiringDrive hiringDrive,
        List<HiringDriveRound> rounds,
        String registrationToken) {
}
