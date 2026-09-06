package com.walkin.repository;

import com.walkin.entity.HiringDrive;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface HiringDriveRepository extends JpaRepository<HiringDrive, Integer> {

    boolean existsByRegistrationTokenHash(String registrationTokenHash);

    @EntityGraph(attributePaths = "company")
    Optional<HiringDrive> findByRegistrationTokenHash(String registrationTokenHash);

    Page<HiringDrive> findByCompany_CompanyId(Integer companyId, Pageable pageable);
}
