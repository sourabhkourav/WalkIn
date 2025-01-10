package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Student")
public class Student{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    private Integer studentId;
    
    @Column(name = "first_name",length = 20, nullable = false)
    private String firstName;
    
    @Column(name = "last_name",length = 20, nullable = false)
    private String lastName;
    
    @Column(name = "email",length = 100, nullable = false, unique = true)
    private String email;
    
    @Column(name = "contact_number",length = 10, nullable = false, unique = true)
    private String contactNumber;
    
    @Column(name = "resume")
    private byte[] resume;
    
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