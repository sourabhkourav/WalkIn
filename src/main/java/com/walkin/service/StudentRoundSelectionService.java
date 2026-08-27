package com.walkin.service;

import com.walkin.entity.StudentRoundSelection;

import java.util.List;
import org.springframework.data.domain.*;

public interface StudentRoundSelectionService {
	StudentRoundSelection createStudentRoundSelection(StudentRoundSelection studentRoundSelection);
	
	StudentRoundSelection getStudentRoundSelectionById(Integer selectionId);

	List<StudentRoundSelection> getAllStudentRoundSelections();
	Page<StudentRoundSelection> getStudentRoundSelections(Pageable pageable);
	
	StudentRoundSelection updateStudentRoundSelection(Integer selectionId, StudentRoundSelection studentRoundSelection);
	
	void deleteStudentRoundSelection(Integer selectionId);
}
