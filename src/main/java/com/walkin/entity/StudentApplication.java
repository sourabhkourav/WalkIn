package com.walkin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "StudentApplication")
public class StudentApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", nullable = false)
    private Integer applicationId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    private Company company;
    
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;
    
}