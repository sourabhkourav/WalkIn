package com.walkin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record HiringDriveSetupRequest(
        @NotNull @Positive Integer companyId,
        @NotBlank @Size(max = 100) String driveName,
        @NotBlank @Size(max = 200) String venue,
        @NotNull @Future OffsetDateTime startsAt,
        @NotNull @Future OffsetDateTime endsAt,
        @NotNull @Valid HiringDriveRegistrationFormRequest registrationForm,
        @NotEmpty @Size(max = 20) List<@NotNull @Positive Integer> companyRoundIds,
        boolean openRegistration) {
}
