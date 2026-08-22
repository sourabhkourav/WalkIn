package com.walkin.controller;

import com.walkin.entity.InterviewRound;
import com.walkin.service.InterviewRoundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview-rounds")
public class InterviewRoundController {

    private final InterviewRoundService interviewRoundService;

    public InterviewRoundController(InterviewRoundService interviewRoundService) {
        this.interviewRoundService = interviewRoundService;
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
    public ResponseEntity<List<InterviewRound>> getAll() {
        return ResponseEntity.ok(interviewRoundService.getAllInterviewRounds());
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
