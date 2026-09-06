package com.walkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompanyRoundDefinitionRequest(
        @NotNull @Positive Integer companyId,
        @NotBlank @Size(max = 100) String roundName,
        @NotBlank @Size(max = 2000) String description) {
}
