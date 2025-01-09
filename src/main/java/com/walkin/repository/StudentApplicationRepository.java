package com.walkin.repository;

import com.walkin.entity.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Integer> {
}
