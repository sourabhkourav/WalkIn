package com.walkin.service;

import com.walkin.entity.StudentApplication;

public interface StudentApplicationService {
	StudentApplication createStudentApplication(StudentApplication studentApplication);
	
	StudentApplication getStudentApplicationById(Integer applicationId);
	
	StudentApplication updateStudentApplication(Integer applicationId, StudentApplication studentApplication);
	
	void deleteStudentApplication(Integer applicationId);
}
