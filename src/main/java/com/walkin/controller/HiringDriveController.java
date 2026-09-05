package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.CreatedHiringDriveResponse;
import com.walkin.dto.HiringDriveRequest;
import com.walkin.dto.HiringDriveResponse;
import com.walkin.dto.HiringDriveStatusRequest;
import com.walkin.dto.PageResponse;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public HiringDriveController(
            HiringDriveService driveService,
            PageRequestFactory pageRequestFactory) {
        this.driveService = driveService;
        this.pageRequestFactory = pageRequestFactory;
    }

    @PostMapping
    public ResponseEntity<CreatedHiringDriveResponse> create(
            @Valid @RequestBody HiringDriveRequest request) {
        HiringDriveCreation creation = driveService.createDrive(request);
        CreatedHiringDriveResponse response = CreatedHiringDriveResponse.from(creation);
        return ResponseEntity
                .created(URI.create("/api/hiring-drives/" + response.drive().driveId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HiringDriveResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(HiringDriveResponse.from(driveService.getDriveById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<HiringDriveResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startsAt") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(driveService.getDrives(
                pageRequestFactory.create(page, size, sort, direction, ALLOWED_SORT_FIELDS))
                .map(HiringDriveResponse::from)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HiringDriveResponse> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody HiringDriveStatusRequest request) {
        return ResponseEntity.ok(HiringDriveResponse.from(
                driveService.updateStatus(id, request.status())));
    }
}
