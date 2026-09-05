package com.walkin.service.impl;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.NotificationChannel;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.exception.ResourceConflictException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.service.CandidateRegistrationService;
import com.walkin.service.HiringDriveService;
import com.walkin.service.ResumeUploadValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class CandidateRegistrationServiceImpl implements CandidateRegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    private final CandidateRegistrationRepository registrationRepository;
    private final HiringDriveService driveService;
    private final ResumeUploadValidator resumeValidator;
    private final Clock clock;

    public CandidateRegistrationServiceImpl(
            CandidateRegistrationRepository registrationRepository,
            HiringDriveService driveService,
            ResumeUploadValidator resumeValidator,
            Clock clock) {
        this.registrationRepository = registrationRepository;
        this.driveService = driveService;
        this.resumeValidator = resumeValidator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CandidateRegistration register(
            String registrationToken,
            CandidateRegistrationRequest request) {
        HiringDrive drive = driveService.getOpenDriveByRegistrationToken(registrationToken);
        String firstName = validateField(
                "firstName", request.getFirstName(), drive.getFirstNameRequirement());
        String lastName = validateField(
                "lastName", request.getLastName(), drive.getLastNameRequirement());
        String email = validateField(
                "email", request.getEmail(), drive.getEmailRequirement());
        String contactNumber = validateField(
                "contactNumber",
                request.getContactNumber(),
                drive.getContactNumberRequirement());
        validateEmail("email", email);
        validatePhone("contactNumber", contactNumber);
        checkDuplicate(drive.getDriveId(), email, contactNumber);

        String notificationDestination = validateNotificationDestination(
                request.getNotificationChannel(), request.getNotificationDestination());
        byte[] resume = resumeValidator.validate(
                request.getResume(), drive.getResumeRequirement());

        CandidateRegistration registration = new CandidateRegistration();
        registration.setRegistrationReference(UUID.randomUUID());
        registration.setHiringDrive(drive);
        registration.setFirstName(firstName);
        registration.setLastName(lastName);
        registration.setEmail(normalizeEmail(email));
        registration.setContactNumber(contactNumber);
        registration.setResume(resume);
        registration.setNotificationChannel(request.getNotificationChannel());
        registration.setNotificationDestination(notificationDestination);
        registration.setAdvanceNoticeMinutes(request.getAdvanceNoticeMinutes());
        registration.setStatus(CandidateRegistrationStatus.WAITING);
        registration.setRegisteredAt(OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        return registrationRepository.save(registration);
    }

    private String validateField(
            String fieldName,
            String rawValue,
            RegistrationFieldRequirement requirement) {
        String value = normalizeBlank(rawValue);
        if (requirement == RegistrationFieldRequirement.HIDDEN && value != null) {
            throw new IllegalArgumentException(
                    fieldName + " is not accepted for this hiring drive");
        }
        if (requirement == RegistrationFieldRequirement.REQUIRED && value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    private void validateEmail(String fieldName, String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(fieldName + " must be a valid email address");
        }
    }

    private void validatePhone(String fieldName, String phoneNumber) {
        if (phoneNumber != null && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must contain 10 to 15 digits with an optional leading +");
        }
    }

    private String validateNotificationDestination(
            NotificationChannel channel,
            String rawDestination) {
        String destination = normalizeBlank(rawDestination);
        if (channel == null || destination == null) {
            throw new IllegalArgumentException(
                    "notificationChannel and notificationDestination are required");
        }
        if (channel == NotificationChannel.EMAIL) {
            validateEmail("notificationDestination", destination);
            return normalizeEmail(destination);
        }
        validatePhone("notificationDestination", destination);
        return destination;
    }

    private void checkDuplicate(Integer driveId, String email, String contactNumber) {
        if (email != null && registrationRepository
                .existsByHiringDrive_DriveIdAndEmailIgnoreCase(driveId, email)) {
            throw new ResourceConflictException(
                    "A candidate with this email is already registered for the hiring drive");
        }
        if (contactNumber != null && registrationRepository
                .existsByHiringDrive_DriveIdAndContactNumber(driveId, contactNumber)) {
            throw new ResourceConflictException(
                    "A candidate with this contact number is already registered for the hiring drive");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
