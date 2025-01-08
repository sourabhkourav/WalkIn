package com.walkin.service.impl;

import com.walkin.entity.Student;
import com.walkin.repository.StudentRepository;
import com.walkin.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class StudentServiceImpl implements StudentService {
	
	@Autowired
	private StudentRepository studentRepository;
	
	
	@Override
	public Student createStudent(Student student) {
		return studentRepository.save(student);
	}
	
	@Override
	public Student getStudentById(Integer studentId) {
		return studentRepository.findById(studentId)
				       .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
	}
	
	@Override
	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}
	
	@Override
	public Student updateStudent(Integer studentId, Student updateStudent) {
		Student student = getStudentById(studentId);
		student.setFirstName(updateStudent.getFirstName());
		student.setLastName(updateStudent.getLastName());
		student.setEmail(updateStudent.getEmail());
		student.setContactNumber(updateStudent.getContactNumber());
		student.setResume(updateStudent.getResume());
		return studentRepository.save(student);
	}
	
	@Override
	public void deleteStudent(Integer studentId) {
		studentRepository.deleteById(studentId);
	}
}
