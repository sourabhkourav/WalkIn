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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "candidate_registration",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_registration_reference",
                        columnNames = "registration_reference"),
                @UniqueConstraint(
                        name = "uk_candidate_registration_drive_email",
                        columnNames = {"drive_id", "email"}),
                @UniqueConstraint(
                        name = "uk_candidate_registration_drive_contact",
                        columnNames = {"drive_id", "contact_number"})
        })
public class CandidateRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id", nullable = false)
    private Integer registrationId;

    @Column(name = "registration_reference", nullable = false, unique = true)
    @NotNull
    private UUID registrationReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    @NotNull
    private HiringDrive hiringDrive;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 100)
    private String email;

    @Column(name = "contact_number", length = 15)
    private String contactNumber;

    @Column(name = "resume")
    private byte[] resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel", length = 20, nullable = false)
    @NotNull
    private NotificationChannel notificationChannel;

    @Column(name = "notification_destination", length = 100, nullable = false)
    @NotBlank
    private String notificationDestination;

    @Column(name = "advance_notice_minutes", nullable = false)
    @Min(5)
    @Max(240)
    private Integer advanceNoticeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @NotNull
    private CandidateRegistrationStatus status = CandidateRegistrationStatus.WAITING;

    @Column(name = "registered_at", nullable = false)
    @NotNull
    private OffsetDateTime registeredAt;

    @Column(name = "status_changed_at", nullable = false)
    @NotNull
    private OffsetDateTime statusChangedAt;

    @Column(name = "status_changed_by", length = 100)
    private String statusChangedBy;

    @Version
    @Column(nullable = false)
    private Long version;

    public Integer getRegistrationId() {
        return registrationId;
    }

    public UUID getRegistrationReference() {
        return registrationReference;
    }

    public void setRegistrationReference(UUID registrationReference) {
        this.registrationReference = registrationReference;
    }

    public HiringDrive getHiringDrive() {
        return hiringDrive;
    }

    public void setHiringDrive(HiringDrive hiringDrive) {
        this.hiringDrive = hiringDrive;
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

    public NotificationChannel getNotificationChannel() {
        return notificationChannel;
    }

    public void setNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public String getNotificationDestination() {
        return notificationDestination;
    }

    public void setNotificationDestination(String notificationDestination) {
        this.notificationDestination = notificationDestination;
    }

    public Integer getAdvanceNoticeMinutes() {
        return advanceNoticeMinutes;
    }

    public void setAdvanceNoticeMinutes(Integer advanceNoticeMinutes) {
        this.advanceNoticeMinutes = advanceNoticeMinutes;
    }

    public CandidateRegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateRegistrationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
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
