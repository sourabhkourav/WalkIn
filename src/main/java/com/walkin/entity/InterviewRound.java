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
    
    public String getRoundName() {
        return roundName;
    }
    
    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}