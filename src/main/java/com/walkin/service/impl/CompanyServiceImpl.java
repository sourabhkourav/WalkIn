package com.walkin.service.impl;

import com.walkin.entity.Company;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyRepository;
import com.walkin.service.CompanyService;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.*;

@Service
public class CompanyServiceImpl implements CompanyService {
	
	private final CompanyRepository companyRepository;

	public CompanyServiceImpl(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
	@Override
	public Company createCompany(Company company) {
		return companyRepository.save(company);
	}
	
	@Override
	public Company getCompanyById(Integer companyId) {
		return companyRepository.findById(companyId)
				       .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));
	}
	
	@Override
	public List<Company> getAllCompanies() {
		return companyRepository.findAll();
	}
	
	@Override
	public Company updateCompany(Integer companyId, Company updateCompany) {
		Company company = getCompanyById(companyId);
		company.setCompanyName(updateCompany.getCompanyName());
		company.setEmail(updateCompany.getEmail());
		company.setContactNumber(updateCompany.getContactNumber());
		company.setJobDescription(updateCompany.getJobDescription());
		return companyRepository.save(company);
	}
	
	@Override
	public void deleteCompany(Integer companyId) {
		companyRepository.delete(getCompanyById(companyId));
	}

	@Override public Page<Company> getCompanies(String query, Pageable pageable) {
		String value = query == null ? "" : query.trim();
		return value.isEmpty() ? companyRepository.findAll(pageable)
				: companyRepository.findByCompanyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(value, value, pageable);
	}
}
