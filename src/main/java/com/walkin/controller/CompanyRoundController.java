package com.walkin.controller;

import com.walkin.dto.CompanyRoundRequest;
import com.walkin.dto.PageResponse;
import com.walkin.config.PageRequestFactory;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.service.CompanyCustomRoundService;
import com.walkin.service.CompanyService;
import com.walkin.service.InterviewRoundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-rounds")
public class CompanyRoundController {

    private final CompanyCustomRoundService companyRoundService;
    private final CompanyService companyService;
    private final InterviewRoundService interviewRoundService;
    private final PageRequestFactory pages;

    public CompanyRoundController(
            CompanyCustomRoundService companyRoundService,
            CompanyService companyService,
            InterviewRoundService interviewRoundService, PageRequestFactory pages) {
        this.companyRoundService = companyRoundService;
        this.companyService = companyService;
        this.interviewRoundService = interviewRoundService;
        this.pages = pages;
    }

    @PostMapping
    public ResponseEntity<CompanyCustomRound> create(@Valid @RequestBody CompanyRoundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyRoundService.createCompanyCustomRound(toEntity(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyCustomRound> get(@PathVariable Integer id) {
        return ResponseEntity.ok(companyRoundService.getCompanyCustomRoundById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CompanyCustomRound>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="companyRoundId") String sort, @RequestParam(defaultValue="asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(companyRoundService.getCompanyCustomRounds(
                pages.create(page,size,sort,direction,java.util.Set.of("companyRoundId")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyCustomRound> update(
            @PathVariable Integer id, @Valid @RequestBody CompanyRoundRequest request) {
        return ResponseEntity.ok(companyRoundService.updateCompanyCustomRound(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        companyRoundService.deleteCompanyCustomRound(id);
        return ResponseEntity.noContent().build();
    }

    private CompanyCustomRound toEntity(CompanyRoundRequest request) {
        CompanyCustomRound companyRound = new CompanyCustomRound();
        companyRound.setCompany(companyService.getCompanyById(request.companyId()));
        companyRound.setInterviewRound(
                interviewRoundService.getInterviewRoundById(request.interviewRoundId()));
        return companyRound;
    }
}
