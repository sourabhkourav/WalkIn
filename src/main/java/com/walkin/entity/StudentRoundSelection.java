package com.walkin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Student_RoundSelections")
public class StudentRoundSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id", nullable = false)
    private Integer selectionId;
    
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;
    
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "company_round_id", referencedColumnName = "company_round_id", nullable = false)
    private CompanyCustomRound companyCustomRound;
    
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SelectionStatus status;
    
    public enum SelectionStatus{
        SELECTED, REJECTED, ON_HOLD
    }
}