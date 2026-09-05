package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.CandidateQueueSummaryResponse;
import com.walkin.dto.CandidateRegistrationDetailsResponse;
import com.walkin.dto.CandidateRegistrationStatusRequest;
import com.walkin.dto.PageResponse;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.service.CandidateRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/hiring-drives/{driveId}/registrations")
public class CandidateRegistrationController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "registrationId", "firstName", "lastName", "registeredAt", "status");

    private final CandidateRegistrationService registrationService;
    private final PageRequestFactory pageRequestFactory;

    public CandidateRegistrationController(
            CandidateRegistrationService registrationService,
            PageRequestFactory pageRequestFactory) {
        this.registrationService = registrationService;
        this.pageRequestFactory = pageRequestFactory;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CandidateRegistrationDetailsResponse>> getAll(
            @PathVariable Integer driveId,
            @RequestParam(required = false) CandidateRegistrationStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registeredAt") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(registrationService.getRegistrations(
                driveId,
                status,
                query,
                pageRequestFactory.create(
                        page, size, sort, direction, ALLOWED_SORT_FIELDS))
                .map(CandidateRegistrationDetailsResponse::from)));
    }

    @GetMapping("/{registrationReference}")
    public ResponseEntity<CandidateRegistrationDetailsResponse> getByReference(
            @PathVariable Integer driveId,
            @PathVariable UUID registrationReference) {
        return ResponseEntity.ok(CandidateRegistrationDetailsResponse.from(
                registrationService.getRegistration(driveId, registrationReference)));
    }

    @GetMapping("/summary")
    public ResponseEntity<CandidateQueueSummaryResponse> getSummary(
            @PathVariable Integer driveId) {
        return ResponseEntity.ok(CandidateQueueSummaryResponse.from(
                registrationService.getStatusCounts(driveId)));
    }

    @GetMapping("/{registrationReference}/resume")
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Integer driveId,
            @PathVariable UUID registrationReference) {
        CandidateRegistration registration =
                registrationService.getRegistration(driveId, registrationReference);
        if (registration.getResume() == null) {
            throw new ResourceNotFoundException("Candidate resume not found");
        }
        String filename = "candidate-" + registrationReference + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(registration.getResume());
    }

    @PatchMapping("/{registrationReference}/status")
    public ResponseEntity<CandidateRegistrationDetailsResponse> updateStatus(
            @PathVariable Integer driveId,
            @PathVariable UUID registrationReference,
            @Valid @RequestBody CandidateRegistrationStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(CandidateRegistrationDetailsResponse.from(
                registrationService.updateStatus(
                        driveId,
                        registrationReference,
                        request.status(),
                        authentication.getName())));
    }
}
