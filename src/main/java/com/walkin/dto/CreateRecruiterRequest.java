package com.walkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRecruiterRequest(
        @NotBlank @Size(min = 3, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "may contain only letters, numbers, dot, underscore, and hyphen")
        String username,
        @NotBlank @Size(min = 12, max = 128) String password) {
}
