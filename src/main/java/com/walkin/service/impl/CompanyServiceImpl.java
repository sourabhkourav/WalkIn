package com.walkin.service.impl;

import com.walkin.entity.Company;
import com.walkin.repository.CompanyRepository;
import com.walkin.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Override
	public Company createCompany(Company company) {
		return companyRepository.save(company);
	}
	
	@Override
	public Company getCompanyById(Integer companyId) {
		return companyRepository.findById(companyId)
				       .orElseThrow(() -> new RuntimeException("Company not found with Id: " + companyId));
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
		companyRepository.deleteById(companyId);
	}
}
