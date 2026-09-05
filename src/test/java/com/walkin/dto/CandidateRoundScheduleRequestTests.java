package com.walkin.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateRoundScheduleRequestTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsPositiveIdsAndFutureReportingTime() {
        CandidateRoundScheduleRequest request = new CandidateRoundScheduleRequest(
                1,
                2,
                OffsetDateTime.now().plusHours(1));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsInvalidIdsAndPastReportingTime() {
        CandidateRoundScheduleRequest request = new CandidateRoundScheduleRequest(
                0,
                -1,
                OffsetDateTime.now().minusMinutes(1));

        Set<ConstraintViolation<CandidateRoundScheduleRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("studentId", "companyRoundId", "reportingTime");
    }

    @Test
    void rejectsMissingFields() {
        CandidateRoundScheduleRequest request = new CandidateRoundScheduleRequest(null, null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("studentId", "companyRoundId", "reportingTime");
    }
}
