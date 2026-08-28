package com.walkin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.OffsetDateTime;

@Entity
@Table(name = "candidate_round_schedule")
public class CandidateRoundSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            referencedColumnName = "student_id",
            nullable = false
    )
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_round_id",
            referencedColumnName = "company_round_id",
            nullable = false
    )
    @NotNull
    private CompanyCustomRound companyRound;

    @Column(name = "reporting_time", nullable = false)
    @NotNull
    private OffsetDateTime reportingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    @Column(name = "notified_at")
    private OffsetDateTime notifiedAt;

    @Column(name = "reported_at")
    private OffsetDateTime reportedAt;

    public Integer getScheduleId() {
        return scheduleId;
    }

    public OffsetDateTime getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(OffsetDateTime reportingTime) {
        this.reportingTime = reportingTime;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public OffsetDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(OffsetDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public OffsetDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(OffsetDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public CompanyCustomRound getCompanyRound() {
        return companyRound;
    }

    public void setCompanyRound(CompanyCustomRound companyRound) {
        this.companyRound = companyRound;
    }
}