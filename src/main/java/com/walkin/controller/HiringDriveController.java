package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.CreatedHiringDriveResponse;
import com.walkin.dto.HiringDriveRequest;
import com.walkin.dto.HiringDriveRegistrationFormRequest;
import com.walkin.dto.HiringDriveRegistrationFormResponse;
import com.walkin.dto.HiringDriveResponse;
import com.walkin.dto.HiringDriveStatusRequest;
import com.walkin.dto.PageResponse;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveService;
import com.walkin.entity.HiringDrive;
import com.walkin.security.CompanyTenantAccess;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/hiring-drives")
public class HiringDriveController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("driveId", "driveName", "startsAt", "endsAt", "status");

    private final HiringDriveService driveService;
    private final PageRequestFactory pageRequestFactory;
    private final CompanyTenantAccess tenantAccess;

    public HiringDriveController(
            HiringDriveService driveService,
            PageRequestFactory pageRequestFactory,
            CompanyTenantAccess tenantAccess) {
        this.driveService = driveService;
        this.pageRequestFactory = pageRequestFactory;
        this.tenantAccess = tenantAccess;
    }

    @PostMapping
    public ResponseEntity<CreatedHiringDriveResponse> create(
            @Valid @RequestBody HiringDriveRequest request,
            Authentication authentication) {
        tenantAccess.requireCompanyAccess(authentication, request.companyId());
        HiringDriveCreation creation = driveService.createDrive(request);
        CreatedHiringDriveResponse response = CreatedHiringDriveResponse.from(creation);
        return ResponseEntity
                .created(URI.create("/api/hiring-drives/" + response.drive().driveId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HiringDriveResponse> getById(
            @PathVariable Integer id, Authentication authentication) {
        HiringDrive drive = authorizedDrive(id, authentication);
        return ResponseEntity.ok(HiringDriveResponse.from(drive));
    }

    @GetMapping
    public ResponseEntity<PageResponse<HiringDriveResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startsAt") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Authentication authentication) {
        var pageable = pageRequestFactory.create(
                page, size, sort, direction, ALLOWED_SORT_FIELDS);
        var drives = tenantAccess.isPlatformAdmin(authentication)
                ? driveService.getDrives(pageable)
                : driveService.getDrives(tenantAccess.requireCompanyId(authentication), pageable);
        return ResponseEntity.ok(PageResponse.from(drives
                .map(HiringDriveResponse::from)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HiringDriveResponse> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody HiringDriveStatusRequest request,
            Authentication authentication) {
        authorizedDrive(id, authentication);
        return ResponseEntity.ok(HiringDriveResponse.from(
                driveService.updateStatus(id, request.status())));
    }

    @GetMapping("/{id}/registration-form")
    public ResponseEntity<HiringDriveRegistrationFormResponse> getRegistrationForm(
            @PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(HiringDriveRegistrationFormResponse.from(
                authorizedDrive(id, authentication)));
    }

    @PutMapping("/{id}/registration-form")
    public ResponseEntity<HiringDriveRegistrationFormResponse> updateRegistrationForm(
            @PathVariable Integer id,
            @Valid @RequestBody HiringDriveRegistrationFormRequest request,
            Authentication authentication) {
        authorizedDrive(id, authentication);
        return ResponseEntity.ok(HiringDriveRegistrationFormResponse.from(
                driveService.updateRegistrationForm(id, request)));
    }

    private HiringDrive authorizedDrive(Integer driveId, Authentication authentication) {
        HiringDrive drive = driveService.getDriveById(driveId);
        tenantAccess.requireCompanyAccess(authentication, drive.getCompany().getCompanyId());
        return drive;
    }
}
