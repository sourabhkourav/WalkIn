package com.walkin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "InterviewRound")
public class InterviewRound{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id", nullable = false)
    private Integer roundId;
    
    @Setter
    @Getter
    @Column(name = "round_name",length = 100, nullable = false)
    private String roundName;
    
    @Setter
    @Getter
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
}