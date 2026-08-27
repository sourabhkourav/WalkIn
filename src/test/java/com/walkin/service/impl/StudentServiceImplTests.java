package com.walkin.service.impl;

import com.walkin.entity.Student;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTests {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createStudentSavesAndReturnsRepositoryResult() {
        Student student = student("Sourabh", "Kourav", "sourabh@example.com", "9876543210");
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        assertSame(student, result);
        verify(studentRepository).save(student);
    }

    @Test
    void getAllStudentsReturnsRepositoryResults() {
        List<Student> students = List.of(
                student("Sourabh", "Kourav", "sourabh@example.com", "9876543210"),
                student("Asha", "Sharma", "asha@example.com", "9876543211"));
        when(studentRepository.findAll()).thenReturn(students);

        assertSame(students, studentService.getAllStudents());
        verify(studentRepository).findAll();
    }

    @Test
    void getStudentThrowsResourceNotFoundWhenIdDoesNotExist() {
        when(studentRepository.findById(42)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.getStudentById(42));

        assertEquals("Student not found with ID: 42", exception.getMessage());
        verify(studentRepository).findById(42);
    }

    @Test
    void updateStudentCopiesEditableFieldsAndSavesExistingEntity() {
        Student existing = student("Old", "Name", "old@example.com", "9876543210");
        Student update = student("New", "Name", "new@example.com", "9876543211");
        update.setResume(new byte[]{1, 2, 3});
        when(studentRepository.findById(7)).thenReturn(Optional.of(existing));
        when(studentRepository.save(existing)).thenReturn(existing);

        Student result = studentService.updateStudent(7, update);

        assertSame(existing, result);
        assertEquals("New", existing.getFirstName());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals(3, existing.getResume().length);
        verify(studentRepository).save(existing);
    }

    @Test
    void deleteStudentDoesNotDeleteWhenIdDoesNotExist() {
        when(studentRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(99));

        verify(studentRepository, never()).delete(any(Student.class));
    }

    private Student student(String firstName, String lastName, String email, String contactNumber) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setContactNumber(contactNumber);
        return student;
    }
}
