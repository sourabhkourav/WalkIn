package com.walkin.dto;

import com.walkin.entity.ApplicationUser.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateUserRequest(
        @NotNull Role role,
        @NotNull Boolean enabled,
        @Positive Integer companyId) {
}
