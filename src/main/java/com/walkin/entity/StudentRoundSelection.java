package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "student_round_selection", uniqueConstraints =
        @UniqueConstraint(name = "uk_student_company_round", columnNames = {"student_id", "company_round_id"}))
public class StudentRoundSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer selectionId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    @NotNull
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "company_round_id", referencedColumnName = "company_round_id", nullable = false)
    @NotNull
    private CompanyCustomRound companyCustomRound;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private SelectionStatus status;
    
    public enum SelectionStatus{
        SELECTED, REJECTED, ON_HOLD
    }

    public Integer getSelectionId() {
        return selectionId;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public CompanyCustomRound getCompanyCustomRound() {
        return companyCustomRound;
    }
    
    public void setCompanyCustomRound(CompanyCustomRound companyCustomRound) {
        this.companyCustomRound = companyCustomRound;
    }
    
    public SelectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SelectionStatus status) {
        this.status = status;
    }
}
