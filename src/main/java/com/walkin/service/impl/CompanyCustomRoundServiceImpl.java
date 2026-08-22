package com.walkin.service.impl;

import com.walkin.entity.CompanyCustomRound;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyCustomRoundRepository;
import com.walkin.service.CompanyCustomRoundService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyCustomRoundServiceImpl implements CompanyCustomRoundService {
	
	private final CompanyCustomRoundRepository companyCustomRoundRepository;

	public CompanyCustomRoundServiceImpl(CompanyCustomRoundRepository companyCustomRoundRepository) {
		this.companyCustomRoundRepository = companyCustomRoundRepository;
	}
	
	@Override
	public CompanyCustomRound createCompanyCustomRound(CompanyCustomRound companyCustomRound) {
		return companyCustomRoundRepository.save(companyCustomRound);
	}
	
	@Override
	public CompanyCustomRound getCompanyCustomRoundById(Integer companyRoundId) {
		return companyCustomRoundRepository.findById(companyRoundId)
				       .orElseThrow(() -> new ResourceNotFoundException("Company round not found with ID: " + companyRoundId));
	}

	@Override
	public List<CompanyCustomRound> getAllCompanyCustomRounds() {
		return companyCustomRoundRepository.findAll();
	}
	
	@Override
	public CompanyCustomRound updateCompanyCustomRound(Integer companyRoundId, CompanyCustomRound updateCompanyCustomRound) {
		CompanyCustomRound companyCustomRound = getCompanyCustomRoundById(companyRoundId);
		companyCustomRound.setCompany(updateCompanyCustomRound.getCompany());
		companyCustomRound.setInterviewRound(updateCompanyCustomRound.getInterviewRound());
		return companyCustomRoundRepository.save(companyCustomRound);
	}
	
	@Override
	public void deleteCompanyCustomRound(Integer companyRoundId) {
		companyCustomRoundRepository.delete(getCompanyCustomRoundById(companyRoundId));
	}
}
