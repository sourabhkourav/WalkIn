package com.walkin.controller;

import com.walkin.dto.PublicHiringDriveResponse;
import com.walkin.service.HiringDriveService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/hiring-drives")
public class PublicHiringDriveController {

    private final HiringDriveService driveService;

    public PublicHiringDriveController(HiringDriveService driveService) {
        this.driveService = driveService;
    }

    @GetMapping("/{registrationToken}")
    @SecurityRequirements
    public ResponseEntity<PublicHiringDriveResponse> getOpenDrive(
            @PathVariable String registrationToken) {
        return ResponseEntity.ok(PublicHiringDriveResponse.from(
                driveService.getOpenDriveByRegistrationToken(registrationToken)));
    }
}
