package com.walkin.service;

import com.walkin.entity.InterviewRound;

import java.util.List;

public interface InterviewRoundService {
	InterviewRound createInterviewRound(InterviewRound interviewRound);
	
	InterviewRound getInterviewRoundById(Integer roundId);

	List<InterviewRound> getAllInterviewRounds();
	
	InterviewRound updateInterviewRound(Integer roundId, InterviewRound interviewRound);
	
	void deleteInterviewRound(Integer roundId);
}
