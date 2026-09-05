package com.walkin.controller;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.dto.CandidateRegistrationResponse;
import com.walkin.dto.PublicHiringDriveResponse;
import com.walkin.service.CandidateRegistrationService;
import com.walkin.service.HiringDriveService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/hiring-drives")
public class PublicHiringDriveController {

    private final HiringDriveService driveService;
    private final CandidateRegistrationService registrationService;

    public PublicHiringDriveController(
            HiringDriveService driveService,
            CandidateRegistrationService registrationService) {
        this.driveService = driveService;
        this.registrationService = registrationService;
    }

    @GetMapping("/{registrationToken}")
    @SecurityRequirements
    public ResponseEntity<PublicHiringDriveResponse> getOpenDrive(
            @PathVariable String registrationToken) {
        return ResponseEntity.ok(PublicHiringDriveResponse.from(
                driveService.getOpenDriveByRegistrationToken(registrationToken)));
    }

    @PostMapping(
            path = "/{registrationToken}/registrations",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirements
    public ResponseEntity<CandidateRegistrationResponse> register(
            @PathVariable String registrationToken,
            @Valid @ModelAttribute CandidateRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CandidateRegistrationResponse.from(
                        registrationService.register(registrationToken, request)));
    }
}
