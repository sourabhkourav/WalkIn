package com.walkin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CandidateRoundScheduleRequest(
        @NotNull @Positive Integer studentId,
        @NotNull @Positive Integer companyRoundId,
        @NotNull @Future OffsetDateTime reportingTime) {
}
