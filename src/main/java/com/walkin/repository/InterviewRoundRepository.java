package com.walkin.repository;

import com.walkin.entity.InterviewRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;

public interface InterviewRoundRepository extends JpaRepository<InterviewRound, Integer> {
    Page<InterviewRound> findByRoundNameContainingIgnoreCase(String roundName, Pageable pageable);
}
