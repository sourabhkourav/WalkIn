package com.walkin.controller;

import com.walkin.entity.Company;
import com.walkin.service.CompanyService;
import com.walkin.config.PageRequestFactory;
import com.walkin.dto.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	private final CompanyService companyService;
	private final PageRequestFactory pages;

	public CompanyController(CompanyService companyService, PageRequestFactory pages) {
		this.companyService = companyService;
		this.pages = pages;
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
	public ResponseEntity<PageResponse<Company>> getCompanies(
			@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
			@RequestParam(defaultValue="companyId") String sort, @RequestParam(defaultValue="asc") String direction,
			@RequestParam(required=false) String query) {
		return ResponseEntity.ok(PageResponse.from(companyService.getCompanies(query,
				pages.create(page, size, sort, direction, java.util.Set.of("companyId", "companyName", "email")))));
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
