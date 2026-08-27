package com.walkin.repository;

import com.walkin.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Page<Company> findByCompanyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String companyName, String email, Pageable pageable);
}
