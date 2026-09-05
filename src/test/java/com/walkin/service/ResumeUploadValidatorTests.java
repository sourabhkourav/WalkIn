package com.walkin.service;

import com.walkin.entity.RegistrationFieldRequirement;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeUploadValidatorTests {

    private final ResumeUploadValidator validator = new ResumeUploadValidator();

    @Test
    void acceptsPdfWithMatchingContentAndSignature() {
        byte[] content = "%PDF-1.7 synthetic".getBytes();
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", content);

        assertThat(validator.validate(resume, RegistrationFieldRequirement.OPTIONAL))
                .isEqualTo(content);
    }

    @Test
    void requiresConfiguredResume() {
        assertThatThrownBy(() -> validator.validate(
                null, RegistrationFieldRequirement.REQUIRED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resume is required");
    }

    @Test
    void rejectsResumeWhenFieldIsHidden() {
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", "%PDF-test".getBytes());

        assertThatThrownBy(() -> validator.validate(
                resume, RegistrationFieldRequirement.HIDDEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resume is not accepted for this hiring drive");
    }

    @Test
    void rejectsMismatchedContentType() {
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "text/plain", "%PDF-test".getBytes());

        assertThatThrownBy(() -> validator.validate(
                resume, RegistrationFieldRequirement.OPTIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resume must be a PDF file");
    }

    @Test
    void rejectsPdfLargerThanTwoMegabytes() {
        byte[] content = new byte[ResumeUploadValidator.MAX_RESUME_BYTES + 1];
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", content);

        assertThatThrownBy(() -> validator.validate(
                resume, RegistrationFieldRequirement.OPTIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resume must not exceed 2 MB");
    }

    @Test
    void rejectsFakePdfContent() {
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", "not-pdf".getBytes());

        assertThatThrownBy(() -> validator.validate(
                resume, RegistrationFieldRequirement.OPTIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resume content is not a valid PDF file");
    }
}
