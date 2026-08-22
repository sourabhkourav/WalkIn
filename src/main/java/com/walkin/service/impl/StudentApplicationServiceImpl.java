package com.walkin.service.impl;

import com.walkin.entity.StudentApplication;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.StudentApplicationRepository;
import com.walkin.service.StudentApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentApplicationServiceImpl implements StudentApplicationService {
	
	private final StudentApplicationRepository studentApplicationRepository;

	public StudentApplicationServiceImpl(StudentApplicationRepository studentApplicationRepository) {
		this.studentApplicationRepository = studentApplicationRepository;
	}
	
	@Override
	public StudentApplication createStudentApplication(StudentApplication studentApplication) {
		return studentApplicationRepository.save(studentApplication);
	}
	
	@Override
	public StudentApplication getStudentApplicationById(Integer applicationId) {
		return studentApplicationRepository.findById(applicationId)
				       .orElseThrow(() -> new ResourceNotFoundException("Student application not found with ID: " + applicationId));
	}

	@Override
	public List<StudentApplication> getAllStudentApplications() {
		return studentApplicationRepository.findAll();
	}
	
	@Override
	public StudentApplication updateStudentApplication(Integer applicationId, StudentApplication updateStudentApplication) {
		StudentApplication studentApplication = getStudentApplicationById(applicationId);
		if (updateStudentApplication.getApplicationDate() != null) {
			studentApplication.setApplicationDate(updateStudentApplication.getApplicationDate());
		}
		studentApplication.setCompany(updateStudentApplication.getCompany());
		studentApplication.setStudent(updateStudentApplication.getStudent());
		return studentApplicationRepository.save(studentApplication);
	}
	
	@Override
	public void deleteStudentApplication(Integer applicationId) {
		studentApplicationRepository.delete(getStudentApplicationById(applicationId));
	}
}
