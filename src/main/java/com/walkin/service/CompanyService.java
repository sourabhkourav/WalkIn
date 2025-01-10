package com.walkin.service;

import com.walkin.entity.Company;

import java.util.List;

public interface CompanyService {
	Company createCompany(Company company);
	
	Company getCompanyById(Integer companyId);
	
	List<Company> getAllCompanies();
	
	Company updateCompany(Integer companyId, Company company);
	
	void deleteCompany(Integer companyId);
}
