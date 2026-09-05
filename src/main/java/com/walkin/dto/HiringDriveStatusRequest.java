package com.walkin.dto;

import com.walkin.entity.HiringDriveStatus;
import jakarta.validation.constraints.NotNull;

public record HiringDriveStatusRequest(@NotNull HiringDriveStatus status) {
}
