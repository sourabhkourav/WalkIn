package com.walkin.service;

import com.walkin.entity.CompanyCustomRound;

import java.util.List;

public interface CompanyCustomRoundService {
	CompanyCustomRound createCompanyCustomRound(CompanyCustomRound companyCustomRound);
	
	CompanyCustomRound getCompanyCustomRoundById(Integer companyRoundId);

	List<CompanyCustomRound> getAllCompanyCustomRounds();
	
	CompanyCustomRound updateCompanyCustomRound(Integer companyRoundId, CompanyCustomRound companyCustomRound);
	
	void deleteCompanyCustomRound(Integer companyRoundId);
}
