package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.CandidateRoundScheduleRequest;
import com.walkin.dto.CandidateRoundScheduleResponse;
import com.walkin.dto.PageResponse;
import com.walkin.service.CandidateRoundScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/candidate-round-schedules")
public class CandidateRoundScheduleController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("scheduleId", "reportingTime", "status");

    private final CandidateRoundScheduleService scheduleService;
    private final PageRequestFactory pageRequestFactory;

    public CandidateRoundScheduleController(
            CandidateRoundScheduleService scheduleService,
            PageRequestFactory pageRequestFactory) {
        this.scheduleService = scheduleService;
        this.pageRequestFactory = pageRequestFactory;
    }

    @PostMapping
    public ResponseEntity<CandidateRoundScheduleResponse> create(
            @Valid @RequestBody CandidateRoundScheduleRequest request) {
        CandidateRoundScheduleResponse response = CandidateRoundScheduleResponse.from(
                scheduleService.createSchedule(request));
        return ResponseEntity
                .created(URI.create("/api/candidate-round-schedules/" + response.scheduleId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateRoundScheduleResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(CandidateRoundScheduleResponse.from(
                scheduleService.getScheduleById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CandidateRoundScheduleResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reportingTime") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(scheduleService.getSchedules(
                pageRequestFactory.create(page, size, sort, direction, ALLOWED_SORT_FIELDS))
                .map(CandidateRoundScheduleResponse::from)));
    }
}
