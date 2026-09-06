package com.walkin.controller;

import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.dto.HiringDriveRoundResponse;
import com.walkin.service.HiringDriveRoundService;
import com.walkin.service.HiringDriveService;
import com.walkin.security.CompanyTenantAccess;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/hiring-drives/{driveId}/rounds")
public class HiringDriveRoundController {

    private final HiringDriveRoundService driveRoundService;
    private final HiringDriveService driveService;
    private final CompanyTenantAccess tenantAccess;

    public HiringDriveRoundController(
            HiringDriveRoundService driveRoundService,
            HiringDriveService driveService,
            CompanyTenantAccess tenantAccess) {
        this.driveRoundService = driveRoundService;
        this.driveService = driveService;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    public ResponseEntity<HiringDriveRoundResponse> addRound(
            @PathVariable Integer driveId,
            @Valid @RequestBody HiringDriveRoundRequest request,
            Authentication authentication) {
        authorizeDrive(driveId, authentication);
        HiringDriveRoundResponse response = HiringDriveRoundResponse.from(
                driveRoundService.addRound(driveId, request));
        return ResponseEntity
                .created(URI.create("/api/hiring-drives/" + driveId
                        + "/rounds/" + response.driveRoundId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<HiringDriveRoundResponse>> getRounds(
            @PathVariable Integer driveId, Authentication authentication) {
        authorizeDrive(driveId, authentication);
        return ResponseEntity.ok(driveRoundService.getRounds(driveId)
                .stream()
                .map(HiringDriveRoundResponse::from)
                .toList());
    }

    private void authorizeDrive(Integer driveId, Authentication authentication) {
        Integer companyId = driveService.getDriveById(driveId).getCompany().getCompanyId();
        tenantAccess.requireCompanyAccess(authentication, companyId);
    }
}
