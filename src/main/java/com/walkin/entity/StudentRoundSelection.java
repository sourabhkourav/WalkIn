package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Student_RoundSelections")
public class StudentRoundSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id", nullable = false)
    private Integer selectionId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "company_round_id", referencedColumnName = "company_round_id", nullable = false)
    private CompanyCustomRound companyCustomRound;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SelectionStatus status;
    
    public enum SelectionStatus{
        SELECTED, REJECTED, ON_HOLD
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