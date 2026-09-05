package com.walkin.repository;

import com.walkin.entity.HiringDriveRound;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HiringDriveRoundRepository extends JpaRepository<HiringDriveRound, Integer> {

    boolean existsByHiringDrive_DriveIdAndCompanyRound_CompanyRoundId(
            Integer driveId,
            Integer companyRoundId);

    boolean existsByHiringDrive_DriveIdAndRoundOrder(Integer driveId, Integer roundOrder);

    @EntityGraph(attributePaths = {"companyRound", "companyRound.interviewRound"})
    List<HiringDriveRound> findByHiringDrive_DriveIdOrderByRoundOrderAsc(Integer driveId);
}
