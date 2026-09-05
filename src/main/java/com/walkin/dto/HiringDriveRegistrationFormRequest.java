package com.walkin.dto;

import com.walkin.entity.RegistrationFieldRequirement;
import jakarta.validation.constraints.NotNull;

public record HiringDriveRegistrationFormRequest(
        @NotNull RegistrationFieldRequirement firstName,
        @NotNull RegistrationFieldRequirement lastName,
        @NotNull RegistrationFieldRequirement email,
        @NotNull RegistrationFieldRequirement contactNumber,
        @NotNull RegistrationFieldRequirement resume) {
}
