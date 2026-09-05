package com.walkin.repository;

import com.walkin.entity.HiringDrive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HiringDriveRepository extends JpaRepository<HiringDrive, Integer> {

    boolean existsByRegistrationTokenHash(String registrationTokenHash);

    Optional<HiringDrive> findByRegistrationTokenHash(String registrationTokenHash);
}
