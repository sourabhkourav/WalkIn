package com.walkin.dto;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CandidateRegistrationDetailsResponse(
        UUID registrationReference,
        String firstName,
        String lastName,
        String email,
        String contactNumber,
        boolean resumeAvailable,
        CandidateRegistrationStatus status,
        OffsetDateTime registeredAt,
        OffsetDateTime statusChangedAt,
        String statusChangedBy) {

    public static CandidateRegistrationDetailsResponse from(CandidateRegistration registration) {
        return new CandidateRegistrationDetailsResponse(
                registration.getRegistrationReference(),
                registration.getFirstName(),
                registration.getLastName(),
                registration.getEmail(),
                registration.getContactNumber(),
                registration.getResume() != null,
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getStatusChangedAt(),
                registration.getStatusChangedBy());
    }
}
