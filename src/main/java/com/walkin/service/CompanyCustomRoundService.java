package com.walkin.service;

import com.walkin.entity.CompanyCustomRound;

import java.util.List;
import org.springframework.data.domain.*;

public interface CompanyCustomRoundService {
	CompanyCustomRound createCompanyCustomRound(CompanyCustomRound companyCustomRound);
	
	CompanyCustomRound getCompanyCustomRoundById(Integer companyRoundId);

	List<CompanyCustomRound> getAllCompanyCustomRounds();
	Page<CompanyCustomRound> getCompanyCustomRounds(Pageable pageable);
	
	CompanyCustomRound updateCompanyCustomRound(Integer companyRoundId, CompanyCustomRound companyCustomRound);
	
	void deleteCompanyCustomRound(Integer companyRoundId);
}
