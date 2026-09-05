package com.walkin.dto;

import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.NotificationChannel;

import java.time.OffsetDateTime;

public record DueCandidateNotificationResponse(
        Integer scheduleId,
        Integer studentId,
        NotificationChannel notificationChannel,
        Integer advanceNoticeMinutes,
        OffsetDateTime notificationDueAt,
        OffsetDateTime reportingTime) {

    public static DueCandidateNotificationResponse from(CandidateRoundSchedule schedule) {
        Integer advanceNoticeMinutes = schedule.getStudent().getAdvanceNoticeMinutes();
        return new DueCandidateNotificationResponse(
                schedule.getScheduleId(),
                schedule.getStudent().getStudentId(),
                schedule.getStudent().getNotificationChannel(),
                advanceNoticeMinutes,
                schedule.getReportingTime().minusMinutes(advanceNoticeMinutes),
                schedule.getReportingTime());
    }
}
