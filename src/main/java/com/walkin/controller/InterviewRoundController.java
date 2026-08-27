package com.walkin.controller;

import com.walkin.entity.InterviewRound;
import com.walkin.service.InterviewRoundService;
import com.walkin.config.PageRequestFactory;
import com.walkin.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview-rounds")
public class InterviewRoundController {

    private final InterviewRoundService interviewRoundService;
    private final PageRequestFactory pages;

    public InterviewRoundController(InterviewRoundService interviewRoundService, PageRequestFactory pages) {
        this.interviewRoundService = interviewRoundService;
        this.pages = pages;
    }

    @PostMapping
    public ResponseEntity<InterviewRound> create(@Valid @RequestBody InterviewRound interviewRound) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interviewRoundService.createInterviewRound(interviewRound));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewRound> get(@PathVariable Integer id) {
        return ResponseEntity.ok(interviewRoundService.getInterviewRoundById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<InterviewRound>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="roundId") String sort, @RequestParam(defaultValue="asc") String direction,
            @RequestParam(required=false) String query) {
        return ResponseEntity.ok(PageResponse.from(interviewRoundService.getInterviewRounds(query,
                pages.create(page, size, sort, direction, java.util.Set.of("roundId", "roundName")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterviewRound> update(
            @PathVariable Integer id, @Valid @RequestBody InterviewRound interviewRound) {
        return ResponseEntity.ok(interviewRoundService.updateInterviewRound(id, interviewRound));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        interviewRoundService.deleteInterviewRound(id);
        return ResponseEntity.noContent().build();
    }
}
