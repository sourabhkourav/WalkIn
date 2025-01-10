package com.walkin.service.impl;

import com.walkin.entity.StudentRoundSelection;
import com.walkin.repository.StudentRoundSelectionRepository;
import com.walkin.service.StudentRoundSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentRoundSelectionServiceImpl implements StudentRoundSelectionService {
	
	@Autowired
	private StudentRoundSelectionRepository studentRoundSelectionRepository;
	
	@Override
	public StudentRoundSelection createStudentRoundSelection(StudentRoundSelection studentRoundSelection) {
		return studentRoundSelectionRepository.save(studentRoundSelection);
	}
	
	@Override
	public StudentRoundSelection getStudentRoundSelectionById(Integer selectionId) {
		return studentRoundSelectionRepository.findById(selectionId)
				       .orElseThrow(() -> new RuntimeException("Student Round Selection not found with Id: " + selectionId));
	}
	
	@Override
	public StudentRoundSelection updateStudentRoundSelection(Integer selectionId, StudentRoundSelection updateStudentRoundSelection) {
		StudentRoundSelection studentRoundSelection = getStudentRoundSelectionById(selectionId);
		studentRoundSelection.setStudent(updateStudentRoundSelection.getStudent());
		studentRoundSelection.setCompanyCustomRound(updateStudentRoundSelection.getCompanyCustomRound());
		studentRoundSelection.setStatus(updateStudentRoundSelection.getStatus());
		return studentRoundSelectionRepository.save(studentRoundSelection);
	}
	
	@Override
	public void deleteStudentRoundSelection(Integer selectionId) {
		studentRoundSelectionRepository.deleteById(selectionId);
	}
}
