package com.walkin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Company")
public class Company{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @Setter
    @Getter
    @Column(name = "company_name",length = 100, nullable = false)
    private String companyName;
    
    @Setter
    @Getter
    @Column(name = "email",length = 100, nullable = false, unique = true)
    private String email;
    
    @Setter
    @Getter
    @Column(name = "contact_number",length = 10, nullable = false, unique = true)
    private String contactNumber;
    
    @Setter
    @Getter
    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;
    
}