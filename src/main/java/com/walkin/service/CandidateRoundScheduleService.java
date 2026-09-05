package com.walkin.service;

import com.walkin.dto.CandidateRoundScheduleRequest;
import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface CandidateRoundScheduleService {

    CandidateRoundSchedule createSchedule(CandidateRoundScheduleRequest request);

    CandidateRoundSchedule getScheduleById(Integer scheduleId);

    Page<CandidateRoundSchedule> getSchedules(Pageable pageable);

    CandidateRoundSchedule reschedule(Integer scheduleId, OffsetDateTime reportingTime);

    CandidateRoundSchedule updateStatus(Integer scheduleId, ScheduleStatus status);
}
