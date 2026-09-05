package com.walkin.dto;

import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CandidateRegistrationResponse(
        UUID registrationReference,
        CandidateRegistrationStatus status,
        OffsetDateTime registeredAt) {

    public static CandidateRegistrationResponse from(CandidateRegistration registration) {
        return new CandidateRegistrationResponse(
                registration.getRegistrationReference(),
                registration.getStatus(),
                registration.getRegisteredAt());
    }
}
