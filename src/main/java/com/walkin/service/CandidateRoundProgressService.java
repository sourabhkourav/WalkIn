package com.walkin.service;

import com.walkin.entity.CandidateRoundProgress;
import com.walkin.entity.RoundProgressStatus;

import java.time.OffsetDateTime;

public interface CandidateRoundProgressService {

    CandidateRoundProgress enqueue(Integer registrationId, Integer driveRoundId, String changedBy);

    CandidateRoundProgress getById(Integer progressId);

    CandidateRoundProgress updateStatus(
            Integer progressId,
            RoundProgressStatus targetStatus,
            OffsetDateTime reportingTime,
            String changedBy);
}
