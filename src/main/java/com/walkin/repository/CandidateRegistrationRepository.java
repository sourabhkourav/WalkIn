package com.walkin.repository;

import com.walkin.entity.CandidateRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRegistrationRepository
        extends JpaRepository<CandidateRegistration, Integer> {

    boolean existsByHiringDrive_DriveIdAndEmailIgnoreCase(Integer driveId, String email);

    boolean existsByHiringDrive_DriveIdAndContactNumber(Integer driveId, String contactNumber);
}
