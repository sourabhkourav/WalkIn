package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "interview_round")
public class InterviewRound{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer roundId;
    
    @Column(name = "round_name",length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String roundName;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String description;

    public Integer getRoundId() {
        return roundId;
    }
    
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
