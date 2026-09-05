package com.walkin;

import com.walkin.entity.Company;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.InterviewRound;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.entity.Student;
import com.walkin.entity.StudentApplication;
import com.walkin.repository.CompanyCustomRoundRepository;
import com.walkin.repository.CompanyRepository;
import com.walkin.repository.HiringDriveRepository;
import com.walkin.repository.HiringDriveRoundRepository;
import com.walkin.repository.InterviewRoundRepository;
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

import java.time.OffsetDateTime;

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
    @Autowired InterviewRoundRepository interviewRounds;
    @Autowired CompanyCustomRoundRepository companyRounds;
    @Autowired HiringDriveRepository hiringDrives;
    @Autowired HiringDriveRoundRepository hiringDriveRounds;

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

    @Test
    void hiringDriveStoresOrderedCompanyRounds() {
        Company company = new Company();
        company.setCompanyName("Drive Integration Company");
        company.setEmail("drive.integration@example.com");
        company.setContactNumber("9876543299");
        company.setJobDescription("Backend Engineer");
        companies.saveAndFlush(company);

        InterviewRound interviewRound = new InterviewRound();
        interviewRound.setRoundName("Drive Technical Round");
        interviewRound.setDescription("Technical interview for the integration test");
        interviewRounds.saveAndFlush(interviewRound);

        CompanyCustomRound companyRound = new CompanyCustomRound();
        companyRound.setCompany(company);
        companyRound.setInterviewRound(interviewRound);
        companyRounds.saveAndFlush(companyRound);

        HiringDrive drive = new HiringDrive();
        drive.setCompany(company);
        drive.setDriveName("Integration Drive");
        drive.setVenue("Integration Venue");
        drive.setStartsAt(OffsetDateTime.parse("2099-01-01T09:00:00Z"));
        drive.setEndsAt(OffsetDateTime.parse("2099-01-01T17:00:00Z"));
        drive.setRegistrationTokenHash(
                "4f2f8d2f79fd789e4c182a45a7300ca522083b652f538c4f16c1c6f65c7d8e21");
        drive.setTokenExpiresAt(drive.getEndsAt());
        hiringDrives.saveAndFlush(drive);

        assertEquals(RegistrationFieldRequirement.REQUIRED, drive.getFirstNameRequirement());
        assertEquals(RegistrationFieldRequirement.HIDDEN, drive.getResumeRequirement());

        HiringDriveRound driveRound = new HiringDriveRound();
        driveRound.setHiringDrive(drive);
        driveRound.setCompanyRound(companyRound);
        driveRound.setRoundOrder(1);
        hiringDriveRounds.saveAndFlush(driveRound);

        assertNotNull(driveRound.getDriveRoundId());
        assertEquals(
                companyRound.getCompanyRoundId(),
                hiringDriveRounds.findByHiringDrive_DriveIdOrderByRoundOrderAsc(
                                drive.getDriveId())
                        .getFirst()
                        .getCompanyRound()
                        .getCompanyRoundId());
    }
}
