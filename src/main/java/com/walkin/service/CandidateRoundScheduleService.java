package com.walkin.service;

import com.walkin.dto.CandidateRoundScheduleRequest;
import com.walkin.entity.CandidateRoundSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CandidateRoundScheduleService {

    CandidateRoundSchedule createSchedule(CandidateRoundScheduleRequest request);

    CandidateRoundSchedule getScheduleById(Integer scheduleId);

    Page<CandidateRoundSchedule> getSchedules(Pageable pageable);
}
