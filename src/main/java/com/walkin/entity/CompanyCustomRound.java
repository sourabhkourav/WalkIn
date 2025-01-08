package com.walkin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CompanyCustomRound")
public class CompanyCustomRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_round_id", nullable = false)
    private Integer companyRoundId;
    
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    private Company company;
    
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "round_id", referencedColumnName = "round_id", nullable = false)
    private InterviewRound interviewRound;
}