package com.walkin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hiring_drive")
public class HiringDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drive_id", nullable = false)
    private Integer driveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull
    private Company company;

    @Column(name = "drive_name", length = 100, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String driveName;

    @Column(length = 200, nullable = false)
    @NotBlank
    @Size(max = 200)
    private String venue;

    @Column(name = "starts_at", nullable = false)
    @NotNull
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    @NotNull
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @NotNull
    private HiringDriveStatus status = HiringDriveStatus.DRAFT;

    @JsonIgnore
    @Column(name = "registration_token_hash", length = 64, nullable = false, unique = true)
    @NotBlank
    private String registrationTokenHash;

    @Column(name = "token_expires_at", nullable = false)
    @NotNull
    private OffsetDateTime tokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_name_requirement", length = 20, nullable = false)
    @NotNull
    private RegistrationFieldRequirement firstNameRequirement =
            RegistrationFieldRequirement.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_name_requirement", length = 20, nullable = false)
    @NotNull
    private RegistrationFieldRequirement lastNameRequirement =
            RegistrationFieldRequirement.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_requirement", length = 20, nullable = false)
    @NotNull
    private RegistrationFieldRequirement emailRequirement =
            RegistrationFieldRequirement.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_number_requirement", length = 20, nullable = false)
    @NotNull
    private RegistrationFieldRequirement contactNumberRequirement =
            RegistrationFieldRequirement.REQUIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "resume_requirement", length = 20, nullable = false)
    @NotNull
    private RegistrationFieldRequirement resumeRequirement =
            RegistrationFieldRequirement.HIDDEN;

    public Integer getDriveId() {
        return driveId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDriveName() {
        return driveName;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public HiringDriveStatus getStatus() {
        return status;
    }

    public void setStatus(HiringDriveStatus status) {
        this.status = status;
    }

    public String getRegistrationTokenHash() {
        return registrationTokenHash;
    }

    public void setRegistrationTokenHash(String registrationTokenHash) {
        this.registrationTokenHash = registrationTokenHash;
    }

    public OffsetDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(OffsetDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public RegistrationFieldRequirement getFirstNameRequirement() {
        return firstNameRequirement;
    }

    public void setFirstNameRequirement(RegistrationFieldRequirement firstNameRequirement) {
        this.firstNameRequirement = firstNameRequirement;
    }

    public RegistrationFieldRequirement getLastNameRequirement() {
        return lastNameRequirement;
    }

    public void setLastNameRequirement(RegistrationFieldRequirement lastNameRequirement) {
        this.lastNameRequirement = lastNameRequirement;
    }

    public RegistrationFieldRequirement getEmailRequirement() {
        return emailRequirement;
    }

    public void setEmailRequirement(RegistrationFieldRequirement emailRequirement) {
        this.emailRequirement = emailRequirement;
    }

    public RegistrationFieldRequirement getContactNumberRequirement() {
        return contactNumberRequirement;
    }

    public void setContactNumberRequirement(
            RegistrationFieldRequirement contactNumberRequirement) {
        this.contactNumberRequirement = contactNumberRequirement;
    }

    public RegistrationFieldRequirement getResumeRequirement() {
        return resumeRequirement;
    }

    public void setResumeRequirement(RegistrationFieldRequirement resumeRequirement) {
        this.resumeRequirement = resumeRequirement;
    }
}
