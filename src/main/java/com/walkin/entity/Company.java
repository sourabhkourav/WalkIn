package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "company")
public class Company{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer companyId;
    
    @Column(name = "company_name",length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String companyName;
    
    @Column(name = "email",length = 100, nullable = false, unique = true)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    
    @Column(name = "contact_number",length = 15, nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "must contain 10 to 15 digits with an optional leading +")
    private String contactNumber;
    
    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String jobDescription;

    public Integer getCompanyId() {
        return companyId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getJobDescription() {
        return jobDescription;
    }
    
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
