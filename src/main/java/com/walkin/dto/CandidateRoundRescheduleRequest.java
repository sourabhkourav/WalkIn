package com.walkin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CandidateRoundRescheduleRequest(
        @NotNull @Future OffsetDateTime reportingTime) {
}
