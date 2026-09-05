package com.walkin.dto;

import com.walkin.entity.HiringDriveRound;

public record HiringDriveRoundResponse(
        Integer driveRoundId,
        Integer driveId,
        Integer companyRoundId,
        Integer interviewRoundId,
        String roundName,
        Integer roundOrder) {

    public static HiringDriveRoundResponse from(HiringDriveRound driveRound) {
        return new HiringDriveRoundResponse(
                driveRound.getDriveRoundId(),
                driveRound.getHiringDrive().getDriveId(),
                driveRound.getCompanyRound().getCompanyRoundId(),
                driveRound.getCompanyRound().getInterviewRound().getRoundId(),
                driveRound.getCompanyRound().getInterviewRound().getRoundName(),
                driveRound.getRoundOrder());
    }
}
