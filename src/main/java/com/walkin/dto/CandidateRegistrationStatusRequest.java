package com.walkin.dto;

import com.walkin.entity.CandidateRegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record CandidateRegistrationStatusRequest(
        @NotNull CandidateRegistrationStatus status) {
}
