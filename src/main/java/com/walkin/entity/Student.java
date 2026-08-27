package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "student")
public class Student{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer studentId;
    
    @Column(name = "first_name",length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @Column(name = "last_name",length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String lastName;
    
    @Column(name = "email",length = 100, nullable = false, unique = true)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    
    @Column(name = "contact_number",length = 15, nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "must contain 10 to 15 digits with an optional leading +")
    private String contactNumber;
    
    @Column(name = "resume")
    private byte[] resume;

    public Integer getStudentId() {
        return studentId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    
    public byte[] getResume() {
        return resume;
    }
    
    public void setResume(byte[] resume) {
        this.resume = resume;
    }
}
