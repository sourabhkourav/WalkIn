package com.walkin.service;

import com.walkin.entity.StudentApplication;

import java.util.List;
import org.springframework.data.domain.*;

public interface StudentApplicationService {
	StudentApplication createStudentApplication(StudentApplication studentApplication);
	
	StudentApplication getStudentApplicationById(Integer applicationId);

	List<StudentApplication> getAllStudentApplications();
	Page<StudentApplication> getStudentApplications(Pageable pageable);
	
	StudentApplication updateStudentApplication(Integer applicationId, StudentApplication studentApplication);
	
	void deleteStudentApplication(Integer applicationId);
}
