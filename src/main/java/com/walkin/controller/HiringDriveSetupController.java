package com.walkin.controller;

import com.walkin.dto.CreatedHiringDriveSetupResponse;
import com.walkin.dto.HiringDriveSetupRequest;
import com.walkin.security.CompanyTenantAccess;
import com.walkin.service.HiringDriveSetup;
import com.walkin.service.HiringDriveSetupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/hiring-drive-setups")
public class HiringDriveSetupController {

    private final HiringDriveSetupService setupService;
    private final CompanyTenantAccess tenantAccess;

    public HiringDriveSetupController(
            HiringDriveSetupService setupService,
            CompanyTenantAccess tenantAccess) {
        this.setupService = setupService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    public ResponseEntity<CreatedHiringDriveSetupResponse> create(
            @Valid @RequestBody HiringDriveSetupRequest request,
            Authentication authentication) {
        tenantAccess.requireCompanyAccess(authentication, request.companyId());
        HiringDriveSetup setup = setupService.create(request);
        return ResponseEntity.created(URI.create(
                        "/api/hiring-drives/" + setup.hiringDrive().getDriveId()))
                .body(CreatedHiringDriveSetupResponse.from(setup));
    }
}
