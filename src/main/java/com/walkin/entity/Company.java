package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Company")
public class Company{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @Column(name = "company_name",length = 100, nullable = false)
    private String companyName;
    
    @Column(name = "email",length = 100, nullable = false, unique = true)
    private String email;
    
    @Column(name = "contact_number",length = 10, nullable = false, unique = true)
    private String contactNumber;
    
    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;
}