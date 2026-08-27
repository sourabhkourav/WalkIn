package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "company_custom_round", uniqueConstraints =
        @UniqueConstraint(name = "uk_company_round", columnNames = {"company_id", "round_id"}))
public class CompanyCustomRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_round_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer companyRoundId;
    
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    @NotNull
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "round_id", referencedColumnName = "round_id", nullable = false)
    @NotNull
    private InterviewRound interviewRound;

    public Integer getCompanyRoundId() {
        return companyRoundId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public InterviewRound getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(InterviewRound interviewRound) {
        this.interviewRound = interviewRound;
    }
}
