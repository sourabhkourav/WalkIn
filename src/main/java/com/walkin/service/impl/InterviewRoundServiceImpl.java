package com.walkin.service.impl;

import com.walkin.entity.InterviewRound;
import com.walkin.repository.InterviewRoundRepository;
import com.walkin.service.InterviewRoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterviewRoundServiceImpl implements InterviewRoundService {
	
	@Autowired
	private InterviewRoundRepository interviewRoundRepository;
	
	@Override
	public InterviewRound createInterviewRound(InterviewRound interviewRound) {
		return interviewRoundRepository.save(interviewRound);
	}
	
	@Override
	public InterviewRound getInterviewRoundById(Integer roundId) {
		return interviewRoundRepository.findById(roundId)
				       .orElseThrow(() -> new RuntimeException("Interview Round not found with Id: " + roundId));
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
		interviewRoundRepository.deleteById(roundId);
	}
}
