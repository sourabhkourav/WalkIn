package com.walkin.dto;

import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.ScheduleStatus;

import java.time.OffsetDateTime;

public record CandidateRoundScheduleResponse(
        Integer scheduleId,
        Integer studentId,
        Integer companyRoundId,
        OffsetDateTime reportingTime,
        ScheduleStatus status,
        OffsetDateTime notifiedAt,
        OffsetDateTime reportedAt) {

    public static CandidateRoundScheduleResponse from(CandidateRoundSchedule schedule) {
        return new CandidateRoundScheduleResponse(
                schedule.getScheduleId(),
                schedule.getStudent().getStudentId(),
                schedule.getCompanyRound().getCompanyRoundId(),
                schedule.getReportingTime(),
                schedule.getStatus(),
                schedule.getNotifiedAt(),
                schedule.getReportedAt());
    }
}
