package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CompanyCustomRound")
public class CompanyCustomRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_round_id", nullable = false)
    private Integer companyRoundId;
    
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "round_id", referencedColumnName = "round_id", nullable = false)
    private InterviewRound interviewRound;
}