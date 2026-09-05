package com.walkin.service;

import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.entity.HiringDriveRound;

import java.util.List;

public interface HiringDriveRoundService {

    HiringDriveRound addRound(Integer driveId, HiringDriveRoundRequest request);

    List<HiringDriveRound> getRounds(Integer driveId);
}
