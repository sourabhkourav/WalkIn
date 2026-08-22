package com.walkin.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StudentApplicationRequest(
        @NotNull Integer studentId,
        @NotNull Integer companyId,
        LocalDateTime applicationDate) {
}
