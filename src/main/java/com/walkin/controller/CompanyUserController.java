package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.CreateRecruiterRequest;
import com.walkin.dto.PageResponse;
import com.walkin.dto.RecruiterStatusRequest;
import com.walkin.dto.UserResponse;
import com.walkin.security.CompanyTenantAccess;
import com.walkin.service.CompanyUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/company-users")
public class CompanyUserController {

    private final CompanyUserService companyUserService;
    private final CompanyTenantAccess tenantAccess;
    private final PageRequestFactory pages;

    public CompanyUserController(
            CompanyUserService companyUserService,
            CompanyTenantAccess tenantAccess,
            PageRequestFactory pages) {
        this.companyUserService = companyUserService;
        this.tenantAccess = tenantAccess;
        this.pages = pages;
    }

    @PostMapping("/recruiters")
    ResponseEntity<UserResponse> createRecruiter(
            @Valid @RequestBody CreateRecruiterRequest request,
            Authentication authentication) {
        Integer companyId = tenantAccess.requireCompanyId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyUserService.createRecruiter(companyId, request));
    }

    @GetMapping
    ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Integer companyId = tenantAccess.requireCompanyId(authentication);
        return ResponseEntity.ok(companyUserService.getUsers(companyId,
                pages.create(page, size, "username", "asc", Set.of("username", "role", "enabled"))));
    }

    @PatchMapping("/recruiters/{userId}/status")
    ResponseEntity<UserResponse> updateRecruiterStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody RecruiterStatusRequest request,
            Authentication authentication) {
        Integer companyId = tenantAccess.requireCompanyId(authentication);
        return ResponseEntity.ok(companyUserService.updateRecruiterStatus(
                companyId, userId, request.enabled()));
    }
}
