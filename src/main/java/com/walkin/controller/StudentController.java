package com.walkin.controller;

import com.walkin.entity.Student;
import com.walkin.service.StudentService;
import com.walkin.config.PageRequestFactory;
import com.walkin.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final PageRequestFactory pages;

    public StudentController(StudentService studentService, PageRequestFactory pages) {
        this.studentService = studentService;
        this.pages = pages;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(student));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Integer id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity<PageResponse<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "studentId") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(PageResponse.from(studentService.getStudents(query,
                pages.create(page, size, sort, direction, java.util.Set.of("studentId", "firstName", "lastName", "email")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Integer id, @Valid @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(id, student);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
