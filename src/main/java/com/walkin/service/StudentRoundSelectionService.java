package com.walkin.service;

import com.walkin.entity.StudentRoundSelection;

public interface StudentRoundSelectionService {
	StudentRoundSelection createStudentRoundSelection(StudentRoundSelection studentRoundSelection);
	
	StudentRoundSelection getStudentRoundSelectionById(Integer selectionId);
	
	StudentRoundSelection updateStudentRoundSelection(Integer selectionId, StudentRoundSelection studentRoundSelection);
	
	void deleteStudentRoundSelection(Integer selectionId);
}
