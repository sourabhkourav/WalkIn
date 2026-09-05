package com.walkin.service;

import com.walkin.entity.RegistrationFieldRequirement;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Component
public class ResumeUploadValidator {

    static final int MAX_RESUME_BYTES = 2 * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};

    public byte[] validate(
            MultipartFile resume,
            RegistrationFieldRequirement requirement) {
        boolean supplied = resume != null && !resume.isEmpty();
        if (requirement == RegistrationFieldRequirement.HIDDEN) {
            if (supplied) {
                throw new IllegalArgumentException("resume is not accepted for this hiring drive");
            }
            return null;
        }
        if (!supplied) {
            if (requirement == RegistrationFieldRequirement.REQUIRED) {
                throw new IllegalArgumentException("resume is required");
            }
            return null;
        }
        if (resume.getSize() > MAX_RESUME_BYTES) {
            throw new IllegalArgumentException("resume must not exceed 2 MB");
        }
        if (!"application/pdf".equalsIgnoreCase(resume.getContentType())) {
            throw new IllegalArgumentException("resume must be a PDF file");
        }

        try {
            byte[] bytes = resume.getBytes();
            if (bytes.length < PDF_SIGNATURE.length
                    || !Arrays.equals(
                            Arrays.copyOf(bytes, PDF_SIGNATURE.length), PDF_SIGNATURE)) {
                throw new IllegalArgumentException("resume content is not a valid PDF file");
            }
            return bytes;
        } catch (IOException exception) {
            throw new IllegalArgumentException("resume could not be read", exception);
        }
    }
}
