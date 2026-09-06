package com.walkin.repository;

import com.walkin.entity.CandidateRoundProgress;
import com.walkin.entity.RoundProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRoundProgressRepository
        extends JpaRepository<CandidateRoundProgress, Integer> {

    boolean existsByRegistration_RegistrationIdAndDriveRound_DriveRoundId(
            Integer registrationId,
            Integer driveRoundId);

    @EntityGraph(attributePaths = {"registration", "driveRound", "driveRound.companyRound",
            "driveRound.companyRound.interviewRound"})
    Page<CandidateRoundProgress> findByDriveRound_DriveRoundIdAndStatus(
            Integer driveRoundId,
            RoundProgressStatus status,
            Pageable pageable);
}
