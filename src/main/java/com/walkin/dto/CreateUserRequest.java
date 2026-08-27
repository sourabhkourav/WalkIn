package com.walkin.dto;

import com.walkin.entity.ApplicationUser.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "may contain only letters, numbers, dot, underscore, and hyphen")
        String username,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotNull Role role) {
}
