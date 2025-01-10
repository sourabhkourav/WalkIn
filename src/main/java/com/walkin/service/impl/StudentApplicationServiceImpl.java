package com.walkin.service.impl;

import com.walkin.entity.StudentApplication;
import com.walkin.repository.StudentApplicationRepository;
import com.walkin.service.StudentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentApplicationServiceImpl implements StudentApplicationService {
	
	@Autowired
	private StudentApplicationRepository studentApplicationRepository;
	
	@Override
	public StudentApplication createStudentApplication(StudentApplication studentApplication) {
		return studentApplicationRepository.save(studentApplication);
	}
	
	@Override
	public StudentApplication getStudentApplicationById(Integer applicationId) {
		return studentApplicationRepository.findById(applicationId)
				       .orElseThrow(() -> new RuntimeException("Student Application not found with Id: " + applicationId));
	}
	
	@Override
	public StudentApplication updateStudentApplication(Integer applicationId, StudentApplication updateStudentApplication) {
		StudentApplication studentApplication = getStudentApplicationById(applicationId);
		studentApplication.setApplicationDate(updateStudentApplication.getApplicationDate());
		studentApplication.setCompany(updateStudentApplication.getCompany());
		studentApplication.setStudent(updateStudentApplication.getStudent());
		return studentApplicationRepository.save(studentApplication);
	}
	
	@Override
	public void deleteStudentApplication(Integer applicationId) {
		studentApplicationRepository.deleteById(applicationId);
	}
}
