package com.walkin.service;

import com.walkin.entity.InterviewRound;

import java.util.List;
import org.springframework.data.domain.*;

public interface InterviewRoundService {
	InterviewRound createInterviewRound(InterviewRound interviewRound);
	
	InterviewRound getInterviewRoundById(Integer roundId);

	List<InterviewRound> getAllInterviewRounds();
	Page<InterviewRound> getInterviewRounds(String query, Pageable pageable);
	
	InterviewRound updateInterviewRound(Integer roundId, InterviewRound interviewRound);
	
	void deleteInterviewRound(Integer roundId);
}
