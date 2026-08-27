package com.walkin.service.impl;

import com.walkin.entity.InterviewRound;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.InterviewRoundRepository;
import com.walkin.service.InterviewRoundService;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.*;

@Service
public class InterviewRoundServiceImpl implements InterviewRoundService {
	
	private final InterviewRoundRepository interviewRoundRepository;

	public InterviewRoundServiceImpl(InterviewRoundRepository interviewRoundRepository) {
		this.interviewRoundRepository = interviewRoundRepository;
	}
	
	@Override
	public InterviewRound createInterviewRound(InterviewRound interviewRound) {
		return interviewRoundRepository.save(interviewRound);
	}
	
	@Override
	public InterviewRound getInterviewRoundById(Integer roundId) {
		return interviewRoundRepository.findById(roundId)
				       .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with ID: " + roundId));
	}

	@Override
	public List<InterviewRound> getAllInterviewRounds() {
		return interviewRoundRepository.findAll();
	}

	@Override public Page<InterviewRound> getInterviewRounds(String query, Pageable pageable) {
		String value = query == null ? "" : query.trim();
		return value.isEmpty() ? interviewRoundRepository.findAll(pageable)
				: interviewRoundRepository.findByRoundNameContainingIgnoreCase(value, pageable);
	}
	
	@Override
	public InterviewRound updateInterviewRound(Integer roundId, InterviewRound updateInterviewRound) {
		InterviewRound interviewRound = getInterviewRoundById(roundId);
		interviewRound.setRoundName(updateInterviewRound.getRoundName());
		interviewRound.setDescription(updateInterviewRound.getDescription());
		return interviewRoundRepository.save(interviewRound);
	}
	
	@Override
	public void deleteInterviewRound(Integer roundId) {
		interviewRoundRepository.delete(getInterviewRoundById(roundId));
	}
}
