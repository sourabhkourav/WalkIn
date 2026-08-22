package com.walkin.controller;

import com.walkin.entity.Company;
import com.walkin.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@PostMapping
	public ResponseEntity<Company> createCompany(@Valid @RequestBody Company company) {
		return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(company));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Company> getCompany(@PathVariable Integer id) {
		return ResponseEntity.ok(companyService.getCompanyById(id));
	}

	@GetMapping
	public ResponseEntity<List<Company>> getCompanies() {
		return ResponseEntity.ok(companyService.getAllCompanies());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Company> updateCompany(
			@PathVariable Integer id, @Valid @RequestBody Company company) {
		return ResponseEntity.ok(companyService.updateCompany(id, company));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCompany(@PathVariable Integer id) {
		companyService.deleteCompany(id);
		return ResponseEntity.noContent().build();
	}
}
