package com.walkin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HiringDriveRoundRequest(
        @NotNull @Positive Integer companyRoundId,
        @NotNull @Positive Integer roundOrder) {
}
