package com.walkin.service;

import com.walkin.entity.StudentRoundSelection;

import java.util.List;

public interface StudentRoundSelectionService {
	StudentRoundSelection createStudentRoundSelection(StudentRoundSelection studentRoundSelection);
	
	StudentRoundSelection getStudentRoundSelectionById(Integer selectionId);

	List<StudentRoundSelection> getAllStudentRoundSelections();
	
	StudentRoundSelection updateStudentRoundSelection(Integer selectionId, StudentRoundSelection studentRoundSelection);
	
	void deleteStudentRoundSelection(Integer selectionId);
}
