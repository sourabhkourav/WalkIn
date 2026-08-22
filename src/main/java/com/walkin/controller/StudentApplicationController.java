package com.walkin.controller;

import com.walkin.dto.StudentApplicationRequest;
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

    public StudentApplicationController(
            StudentApplicationService applicationService,
            StudentService studentService,
            CompanyService companyService) {
        this.applicationService = applicationService;
        this.studentService = studentService;
        this.companyService = companyService;
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
    public ResponseEntity<List<StudentApplication>> getAll() {
        return ResponseEntity.ok(applicationService.getAllStudentApplications());
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
