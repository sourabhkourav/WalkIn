package com.walkin.repository;

import com.walkin.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email, Pageable pageable);
}
