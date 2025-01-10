package com.walkin.service;

import com.walkin.entity.InterviewRound;

public interface InterviewRoundService {
	InterviewRound createInterviewRound(InterviewRound interviewRound);
	
	InterviewRound getInterviewRoundById(Integer roundId);
	
	InterviewRound updateInterviewRound(Integer roundId, InterviewRound interviewRound);
	
	void deleteInterviewRound(Integer roundId);
}
