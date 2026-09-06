package com.walkin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "candidate_round_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_candidate_round_progress",
                columnNames = {"registration_id", "drive_round_id"}))
public class CandidateRoundProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id", nullable = false)
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    @NotNull
    private CandidateRegistration registration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_round_id", nullable = false)
    @NotNull
    private HiringDriveRound driveRound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private RoundProgressStatus status = RoundProgressStatus.WAITING;

    @Column(name = "reporting_time")
    private OffsetDateTime reportingTime;

    @Column(name = "queued_at", nullable = false)
    @NotNull
    private OffsetDateTime queuedAt;

    @Column(name = "called_at")
    private OffsetDateTime calledAt;

    @Column(name = "interview_started_at")
    private OffsetDateTime interviewStartedAt;

    @Column(name = "interview_completed_at")
    private OffsetDateTime interviewCompletedAt;

    @Column(name = "result_decided_at")
    private OffsetDateTime resultDecidedAt;

    @Column(name = "result_decided_by", length = 100)
    private String resultDecidedBy;

    @Column(name = "status_changed_at", nullable = false)
    @NotNull
    private OffsetDateTime statusChangedAt;

    @Column(name = "status_changed_by", nullable = false, length = 100)
    @NotBlank
    private String statusChangedBy;

    @Version
    @Column(nullable = false)
    private Long version;

    public Integer getProgressId() {
        return progressId;
    }

    public CandidateRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(CandidateRegistration registration) {
        this.registration = registration;
    }

    public HiringDriveRound getDriveRound() {
        return driveRound;
    }

    public void setDriveRound(HiringDriveRound driveRound) {
        this.driveRound = driveRound;
    }

    public RoundProgressStatus getStatus() {
        return status;
    }

    public void setStatus(RoundProgressStatus status) {
        this.status = status;
    }

    public OffsetDateTime getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(OffsetDateTime reportingTime) {
        this.reportingTime = reportingTime;
    }

    public OffsetDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(OffsetDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    public OffsetDateTime getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(OffsetDateTime calledAt) {
        this.calledAt = calledAt;
    }

    public OffsetDateTime getInterviewStartedAt() {
        return interviewStartedAt;
    }

    public void setInterviewStartedAt(OffsetDateTime interviewStartedAt) {
        this.interviewStartedAt = interviewStartedAt;
    }

    public OffsetDateTime getInterviewCompletedAt() {
        return interviewCompletedAt;
    }

    public void setInterviewCompletedAt(OffsetDateTime interviewCompletedAt) {
        this.interviewCompletedAt = interviewCompletedAt;
    }

    public OffsetDateTime getResultDecidedAt() {
        return resultDecidedAt;
    }

    public void setResultDecidedAt(OffsetDateTime resultDecidedAt) {
        this.resultDecidedAt = resultDecidedAt;
    }

    public String getResultDecidedBy() {
        return resultDecidedBy;
    }

    public void setResultDecidedBy(String resultDecidedBy) {
        this.resultDecidedBy = resultDecidedBy;
    }

    public OffsetDateTime getStatusChangedAt() {
        return statusChangedAt;
    }

    public void setStatusChangedAt(OffsetDateTime statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    public String getStatusChangedBy() {
        return statusChangedBy;
    }

    public void setStatusChangedBy(String statusChangedBy) {
        this.statusChangedBy = statusChangedBy;
    }

    public Long getVersion() {
        return version;
    }
}
