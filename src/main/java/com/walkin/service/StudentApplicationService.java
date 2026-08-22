package com.walkin.service;

import com.walkin.entity.StudentApplication;

import java.util.List;

public interface StudentApplicationService {
	StudentApplication createStudentApplication(StudentApplication studentApplication);
	
	StudentApplication getStudentApplicationById(Integer applicationId);

	List<StudentApplication> getAllStudentApplications();
	
	StudentApplication updateStudentApplication(Integer applicationId, StudentApplication studentApplication);
	
	void deleteStudentApplication(Integer applicationId);
}
