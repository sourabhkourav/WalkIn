package com.walkin;

import com.walkin.entity.Company;
import com.walkin.entity.Student;
import com.walkin.entity.StudentApplication;
import com.walkin.repository.CompanyRepository;
import com.walkin.repository.StudentApplicationRepository;
import com.walkin.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlIntegrationTests {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.bootstrap-admin.username", () -> "integration-admin");
        registry.add("app.bootstrap-admin.password", () -> "integration-password");
        registry.add("app.jwt.secret", () -> "integration-jwt-secret-that-is-over-32-bytes");
    }

    @Autowired StudentRepository students;
    @Autowired CompanyRepository companies;
    @Autowired StudentApplicationRepository applications;

    @Test
    void flywaySchemaPersistsRelationshipsAndEnforcesUniqueApplication() {
        Student student = new Student(); student.setFirstName("Asha"); student.setLastName("Sharma");
        student.setEmail("asha.integration@example.com"); student.setContactNumber("9876543210"); students.saveAndFlush(student);
        Company company = new Company(); company.setCompanyName("Acme"); company.setEmail("jobs.integration@acme.com");
        company.setContactNumber("9876543211"); company.setJobDescription("Engineer"); companies.saveAndFlush(company);
        StudentApplication first = new StudentApplication(); first.setStudent(student); first.setCompany(company); applications.saveAndFlush(first);
        assertNotNull(first.getApplicationId()); assertNotNull(first.getApplicationDate());
        StudentApplication duplicate = new StudentApplication(); duplicate.setStudent(student); duplicate.setCompany(company);
        assertThrows(RuntimeException.class, () -> applications.saveAndFlush(duplicate));
    }
}
