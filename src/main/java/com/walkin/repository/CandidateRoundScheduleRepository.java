package com.walkin.repository;

import com.walkin.entity.CandidateRoundSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRoundScheduleRepository
        extends JpaRepository<CandidateRoundSchedule, Integer> {
}
