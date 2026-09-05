package com.walkin.service;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;

public interface CandidateRegistrationService {

    CandidateRegistration register(String registrationToken, CandidateRegistrationRequest request);
}
