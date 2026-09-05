package com.walkin.dto;

import com.walkin.entity.CandidateRegistrationStatus;

import java.util.Map;

public record CandidateQueueSummaryResponse(
        long waiting,
        long called,
        long completed,
        long withdrawn,
        long total) {

    public static CandidateQueueSummaryResponse from(
            Map<CandidateRegistrationStatus, Long> counts) {
        long waiting = counts.getOrDefault(CandidateRegistrationStatus.WAITING, 0L);
        long called = counts.getOrDefault(CandidateRegistrationStatus.CALLED, 0L);
        long completed = counts.getOrDefault(CandidateRegistrationStatus.COMPLETED, 0L);
        long withdrawn = counts.getOrDefault(CandidateRegistrationStatus.WITHDRAWN, 0L);
        return new CandidateQueueSummaryResponse(
                waiting, called, completed, withdrawn,
                waiting + called + completed + withdrawn);
    }
}
