package com.walkin.dto;

import jakarta.validation.constraints.NotNull;

public record RecruiterStatusRequest(@NotNull Boolean enabled) {
}
