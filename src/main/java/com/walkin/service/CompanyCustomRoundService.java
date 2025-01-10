package com.walkin.service;

import com.walkin.entity.CompanyCustomRound;

public interface CompanyCustomRoundService {
	CompanyCustomRound createCompanyCustomRound(CompanyCustomRound companyCustomRound);
	
	CompanyCustomRound getCompanyCustomRoundById(Integer companyRoundId);
	
	CompanyCustomRound updateCompanyCustomRound(Integer companyRoundId, CompanyCustomRound companyCustomRound);
	
	void deleteCompanyCustomRound(Integer companyRoundId);
}
