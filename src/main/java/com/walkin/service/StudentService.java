package com.walkin.service;

import com.walkin.entity.Student;

import java.util.List;
import org.springframework.data.domain.*;

public interface StudentService {
	Student createStudent(Student student);
	
	Student getStudentById(Integer studentId);
	
	List<Student> getAllStudents();
	Page<Student> getStudents(String query, Pageable pageable);
	
	Student updateStudent(Integer studentId, Student student);
	
	void deleteStudent(Integer studentId);
}
