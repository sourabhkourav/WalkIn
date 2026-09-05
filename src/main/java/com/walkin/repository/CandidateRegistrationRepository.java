package com.walkin.repository;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRegistrationRepository
        extends JpaRepository<CandidateRegistration, Integer>,
        JpaSpecificationExecutor<CandidateRegistration> {

    boolean existsByHiringDrive_DriveIdAndEmailIgnoreCase(Integer driveId, String email);

    boolean existsByHiringDrive_DriveIdAndContactNumber(Integer driveId, String contactNumber);

    Optional<CandidateRegistration> findByHiringDrive_DriveIdAndRegistrationReference(
            Integer driveId,
            UUID registrationReference);

    long countByHiringDrive_DriveIdAndStatus(
            Integer driveId,
            CandidateRegistrationStatus status);
}
