package com.walkin.dto;

import com.walkin.entity.StudentRoundSelection.SelectionStatus;
import jakarta.validation.constraints.NotNull;

public record StudentRoundSelectionRequest(
        @NotNull Integer studentId,
        @NotNull Integer companyRoundId,
        @NotNull SelectionStatus status) {
}
