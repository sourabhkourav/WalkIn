package com.walkin.service;

import com.walkin.entity.Student;

import java.util.List;

public interface StudentService {
	Student createStudent(Student student);
	
	Student getStudentById(Integer studentId);
	
	List<Student> getAllStudents();
	
	Student updateStudent(Integer studentId, Student student);
	
	void deleteStudent(Integer studentId);
}
