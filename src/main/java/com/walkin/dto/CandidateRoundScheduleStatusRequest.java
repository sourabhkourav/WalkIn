package com.walkin.dto;

import com.walkin.entity.ScheduleStatus;
import jakarta.validation.constraints.NotNull;

public record CandidateRoundScheduleStatusRequest(
        @NotNull ScheduleStatus status) {
}
