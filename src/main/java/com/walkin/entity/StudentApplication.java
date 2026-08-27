package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_application", uniqueConstraints =
        @UniqueConstraint(name = "uk_student_company_application", columnNames = {"student_id", "company_id"}))
public class StudentApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer applicationId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    @NotNull
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    @NotNull
    private Company company;
    
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @PrePersist
    void setDefaultApplicationDate() {
        if (applicationDate == null) {
            applicationDate = LocalDateTime.now();
        }
    }

    public Integer getApplicationId() {
        return applicationId;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
}
