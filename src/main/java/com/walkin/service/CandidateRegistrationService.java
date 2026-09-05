package com.walkin.service;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface CandidateRegistrationService {

    CandidateRegistration register(String registrationToken, CandidateRegistrationRequest request);

    Page<CandidateRegistration> getRegistrations(
            Integer driveId,
            CandidateRegistrationStatus status,
            String query,
            Pageable pageable);

    CandidateRegistration getRegistration(Integer driveId, UUID registrationReference);

    Map<CandidateRegistrationStatus, Long> getStatusCounts(Integer driveId);

    CandidateRegistration updateStatus(
            Integer driveId,
            UUID registrationReference,
            CandidateRegistrationStatus targetStatus,
            String changedBy);
}
