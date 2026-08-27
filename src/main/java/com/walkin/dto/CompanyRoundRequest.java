package com.walkin.dto;

import jakarta.validation.constraints.NotNull;

public record CompanyRoundRequest(
        @NotNull Integer companyId,
        @NotNull Integer interviewRoundId) {
}
