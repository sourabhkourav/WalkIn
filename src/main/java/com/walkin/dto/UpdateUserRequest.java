package com.walkin.dto;

import com.walkin.entity.ApplicationUser.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(@NotNull Role role, @NotNull Boolean enabled) {
}
