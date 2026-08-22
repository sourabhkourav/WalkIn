package com.walkin.service.impl;

import com.walkin.entity.StudentRoundSelection;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.StudentRoundSelectionRepository;
import com.walkin.service.StudentRoundSelectionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentRoundSelectionServiceImpl implements StudentRoundSelectionService {
	
	private final StudentRoundSelectionRepository studentRoundSelectionRepository;

	public StudentRoundSelectionServiceImpl(StudentRoundSelectionRepository studentRoundSelectionRepository) {
		this.studentRoundSelectionRepository = studentRoundSelectionRepository;
	}
	
	@Override
	public StudentRoundSelection createStudentRoundSelection(StudentRoundSelection studentRoundSelection) {
		return studentRoundSelectionRepository.save(studentRoundSelection);
	}
	
	@Override
	public StudentRoundSelection getStudentRoundSelectionById(Integer selectionId) {
		return studentRoundSelectionRepository.findById(selectionId)
				       .orElseThrow(() -> new ResourceNotFoundException("Student round selection not found with ID: " + selectionId));
	}

	@Override
	public List<StudentRoundSelection> getAllStudentRoundSelections() {
		return studentRoundSelectionRepository.findAll();
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
		studentRoundSelectionRepository.delete(getStudentRoundSelectionById(selectionId));
	}
}
