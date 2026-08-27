package com.walkin.service;

import com.walkin.entity.Company;

import java.util.List;
import org.springframework.data.domain.*;

public interface CompanyService {
	Company createCompany(Company company);
	
	Company getCompanyById(Integer companyId);
	
	List<Company> getAllCompanies();
	Page<Company> getCompanies(String query, Pageable pageable);
	
	Company updateCompany(Integer companyId, Company company);
	
	void deleteCompany(Integer companyId);
}
