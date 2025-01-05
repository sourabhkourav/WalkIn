package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "InterviewRound")
public class InterviewRound{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id", nullable = false)
    private Integer roundId;
    
    @Column(name = "round_name",length = 100, nullable = false)
    private String roundName;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
}