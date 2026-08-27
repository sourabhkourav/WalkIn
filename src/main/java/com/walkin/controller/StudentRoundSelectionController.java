package com.walkin.controller;

import com.walkin.dto.StudentRoundSelectionRequest;
import com.walkin.dto.PageResponse;
import com.walkin.config.PageRequestFactory;
import com.walkin.entity.StudentRoundSelection;
import com.walkin.service.CompanyCustomRoundService;
import com.walkin.service.StudentRoundSelectionService;
import com.walkin.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/round-selections")
public class StudentRoundSelectionController {

    private final StudentRoundSelectionService selectionService;
    private final StudentService studentService;
    private final CompanyCustomRoundService companyRoundService;
    private final PageRequestFactory pages;

    public StudentRoundSelectionController(
            StudentRoundSelectionService selectionService,
            StudentService studentService,
            CompanyCustomRoundService companyRoundService, PageRequestFactory pages) {
        this.selectionService = selectionService;
        this.studentService = studentService;
        this.companyRoundService = companyRoundService;
        this.pages = pages;
    }

    @PostMapping
    public ResponseEntity<StudentRoundSelection> create(
            @Valid @RequestBody StudentRoundSelectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(selectionService.createStudentRoundSelection(toEntity(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentRoundSelection> get(@PathVariable Integer id) {
        return ResponseEntity.ok(selectionService.getStudentRoundSelectionById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<StudentRoundSelection>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="selectionId") String sort, @RequestParam(defaultValue="asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(selectionService.getStudentRoundSelections(
                pages.create(page,size,sort,direction,java.util.Set.of("selectionId","status")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentRoundSelection> update(
            @PathVariable Integer id, @Valid @RequestBody StudentRoundSelectionRequest request) {
        return ResponseEntity.ok(selectionService.updateStudentRoundSelection(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        selectionService.deleteStudentRoundSelection(id);
        return ResponseEntity.noContent().build();
    }

    private StudentRoundSelection toEntity(StudentRoundSelectionRequest request) {
        StudentRoundSelection selection = new StudentRoundSelection();
        selection.setStudent(studentService.getStudentById(request.studentId()));
        selection.setCompanyCustomRound(
                companyRoundService.getCompanyCustomRoundById(request.companyRoundId()));
        selection.setStatus(request.status());
        return selection;
    }
}
