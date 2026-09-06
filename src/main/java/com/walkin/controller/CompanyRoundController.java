package com.walkin.controller;

import com.walkin.dto.CompanyRoundRequest;
import com.walkin.dto.CompanyRoundDefinitionRequest;
import com.walkin.dto.PageResponse;
import com.walkin.config.PageRequestFactory;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.service.CompanyCustomRoundService;
import com.walkin.service.CompanyService;
import com.walkin.service.InterviewRoundService;
import com.walkin.service.CompanyRoundDefinitionService;
import com.walkin.security.CompanyTenantAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-rounds")
public class CompanyRoundController {

    private final CompanyCustomRoundService companyRoundService;
    private final CompanyService companyService;
    private final InterviewRoundService interviewRoundService;
    private final PageRequestFactory pages;
    private final CompanyTenantAccess tenantAccess;
    private final CompanyRoundDefinitionService definitionService;

    public CompanyRoundController(
            CompanyCustomRoundService companyRoundService,
            CompanyService companyService,
            InterviewRoundService interviewRoundService,
            PageRequestFactory pages,
            CompanyTenantAccess tenantAccess,
            CompanyRoundDefinitionService definitionService) {
        this.companyRoundService = companyRoundService;
        this.companyService = companyService;
        this.interviewRoundService = interviewRoundService;
        this.pages = pages;
        this.tenantAccess = tenantAccess;
        this.definitionService = definitionService;
    }

    @PostMapping
    public ResponseEntity<CompanyCustomRound> create(
            @Valid @RequestBody CompanyRoundRequest request,
            Authentication authentication) {
        tenantAccess.requireCompanyAccess(authentication, request.companyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyRoundService.createCompanyCustomRound(toEntity(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyCustomRound> get(
            @PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(authorizedRound(id, authentication));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CompanyCustomRound>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="companyRoundId") String sort,
            @RequestParam(defaultValue="asc") String direction,
            Authentication authentication) {
        var pageable = pages.create(
                page, size, sort, direction, java.util.Set.of("companyRoundId"));
        var rounds = tenantAccess.isPlatformAdmin(authentication)
                ? companyRoundService.getCompanyCustomRounds(pageable)
                : companyRoundService.getCompanyCustomRounds(
                        tenantAccess.requireCompanyId(authentication), pageable);
        return ResponseEntity.ok(PageResponse.from(rounds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyCustomRound> update(
            @PathVariable Integer id,
            @Valid @RequestBody CompanyRoundRequest request,
            Authentication authentication) {
        authorizedRound(id, authentication);
        tenantAccess.requireCompanyAccess(authentication, request.companyId());
        return ResponseEntity.ok(companyRoundService.updateCompanyCustomRound(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id, Authentication authentication) {
        authorizedRound(id, authentication);
        companyRoundService.deleteCompanyCustomRound(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custom")
    public ResponseEntity<CompanyCustomRound> createCustom(
            @Valid @RequestBody CompanyRoundDefinitionRequest request,
            Authentication authentication) {
        tenantAccess.requireCompanyAccess(authentication, request.companyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(definitionService.create(request));
    }

    private CompanyCustomRound authorizedRound(Integer id, Authentication authentication) {
        CompanyCustomRound round = companyRoundService.getCompanyCustomRoundById(id);
        tenantAccess.requireCompanyAccess(
                authentication, round.getCompany().getCompanyId());
        return round;
    }

    private CompanyCustomRound toEntity(CompanyRoundRequest request) {
        CompanyCustomRound companyRound = new CompanyCustomRound();
        companyRound.setCompany(companyService.getCompanyById(request.companyId()));
        companyRound.setInterviewRound(
                interviewRoundService.getInterviewRoundById(request.interviewRoundId()));
        return companyRound;
    }
}
