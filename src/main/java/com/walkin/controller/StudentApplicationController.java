package com.walkin.controller;

import com.walkin.dto.StudentApplicationRequest;
import com.walkin.dto.PageResponse;
import com.walkin.config.PageRequestFactory;
import com.walkin.entity.StudentApplication;
import com.walkin.service.CompanyService;
import com.walkin.service.StudentApplicationService;
import com.walkin.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class StudentApplicationController {

    private final StudentApplicationService applicationService;
    private final StudentService studentService;
    private final CompanyService companyService;
    private final PageRequestFactory pages;

    public StudentApplicationController(
            StudentApplicationService applicationService,
            StudentService studentService,
            CompanyService companyService, PageRequestFactory pages) {
        this.applicationService = applicationService;
        this.studentService = studentService;
        this.companyService = companyService;
        this.pages = pages;
    }

    @PostMapping
    public ResponseEntity<StudentApplication> create(
            @Valid @RequestBody StudentApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createStudentApplication(toEntity(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentApplication> get(@PathVariable Integer id) {
        return ResponseEntity.ok(applicationService.getStudentApplicationById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<StudentApplication>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="applicationId") String sort, @RequestParam(defaultValue="asc") String direction) {
        return ResponseEntity.ok(PageResponse.from(applicationService.getStudentApplications(
                pages.create(page,size,sort,direction,java.util.Set.of("applicationId","applicationDate")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentApplication> update(
            @PathVariable Integer id, @Valid @RequestBody StudentApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateStudentApplication(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        applicationService.deleteStudentApplication(id);
        return ResponseEntity.noContent().build();
    }

    private StudentApplication toEntity(StudentApplicationRequest request) {
        StudentApplication application = new StudentApplication();
        application.setStudent(studentService.getStudentById(request.studentId()));
        application.setCompany(companyService.getCompanyById(request.companyId()));
        application.setApplicationDate(request.applicationDate());
        return application;
    }
}
