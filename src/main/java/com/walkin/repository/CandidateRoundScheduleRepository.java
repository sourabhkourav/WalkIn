package com.walkin.repository;

import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface CandidateRoundScheduleRepository
        extends JpaRepository<CandidateRoundSchedule, Integer> {

    boolean existsByStudent_StudentIdAndCompanyRound_CompanyRoundId(
            Integer studentId,
            Integer companyRoundId);

    @EntityGraph(attributePaths = "student")
    List<CandidateRoundSchedule> findByStatusAndReportingTimeBetweenOrderByReportingTimeAsc(
            ScheduleStatus status,
            OffsetDateTime windowStart,
            OffsetDateTime windowEnd);
}
