package com.walkin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Student")
public class Student{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id", nullable = false)
    private Integer studentId;
    
    @Setter
    @Getter
    @Column(name = "first_name",length = 20, nullable = false)
    private String firstName;
    
    @Setter
    @Getter
    @Column(name = "last_name",length = 20, nullable = false)
    private String lastName;
    
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
    @Column(name = "resume")
    private byte[] resume;
}