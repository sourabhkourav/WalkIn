package com.walkin.repository;

import com.walkin.entity.CompanyCustomRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyCustomRoundRepository extends JpaRepository<CompanyCustomRound, Integer> {
    Page<CompanyCustomRound> findByCompany_CompanyId(Integer companyId, Pageable pageable);
}
