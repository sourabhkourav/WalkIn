package com.walkin.service.impl;

import com.walkin.dto.CandidateRegistrationRequest;
import com.walkin.entity.CandidateRegistration;
import com.walkin.entity.CandidateRegistrationStatus;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.NotificationChannel;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CandidateRegistrationRepository;
import com.walkin.service.CandidateRegistrationService;
import com.walkin.service.HiringDriveService;
import com.walkin.service.ResumeUploadValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class CandidateRegistrationServiceImpl implements CandidateRegistrationService {

    private static final int MAX_SEARCH_LENGTH = 100;
    private static final Map<CandidateRegistrationStatus, Set<CandidateRegistrationStatus>>
            ALLOWED_TRANSITIONS = Map.of(
                    CandidateRegistrationStatus.WAITING,
                    Set.of(CandidateRegistrationStatus.CALLED,
                            CandidateRegistrationStatus.WITHDRAWN),
                    CandidateRegistrationStatus.CALLED,
                    Set.of(CandidateRegistrationStatus.WAITING,
                            CandidateRegistrationStatus.COMPLETED,
                            CandidateRegistrationStatus.WITHDRAWN),
                    CandidateRegistrationStatus.COMPLETED, Set.of(),
                    CandidateRegistrationStatus.WITHDRAWN, Set.of());

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
        OffsetDateTime registeredAt = now();
        registration.setRegisteredAt(registeredAt);
        registration.setStatusChangedAt(registeredAt);
        return registrationRepository.save(registration);
    }

    @Override
    public Page<CandidateRegistration> getRegistrations(
            Integer driveId,
            CandidateRegistrationStatus status,
            String query,
            Pageable pageable) {
        requireDrive(driveId);
        String searchTerm = normalizeSearch(query);
        Specification<CandidateRegistration> specification = (root, criteriaQuery, builder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("hiringDrive").get("driveId"), driveId));
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (searchTerm != null) {
                String pattern = "%" + searchTerm.toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("firstName")), pattern),
                        builder.like(builder.lower(root.get("lastName")), pattern),
                        builder.like(builder.lower(root.get("email")), pattern),
                        builder.like(root.get("contactNumber"), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return registrationRepository.findAll(specification, pageable);
    }

    @Override
    public CandidateRegistration getRegistration(
            Integer driveId,
            UUID registrationReference) {
        requireDrive(driveId);
        return registrationRepository
                .findByHiringDrive_DriveIdAndRegistrationReference(
                        driveId, registrationReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate registration not found"));
    }

    @Override
    public Map<CandidateRegistrationStatus, Long> getStatusCounts(Integer driveId) {
        requireDrive(driveId);
        EnumMap<CandidateRegistrationStatus, Long> counts =
                new EnumMap<>(CandidateRegistrationStatus.class);
        for (CandidateRegistrationStatus status : CandidateRegistrationStatus.values()) {
            counts.put(status,
                    registrationRepository.countByHiringDrive_DriveIdAndStatus(driveId, status));
        }
        return counts;
    }

    @Override
    @Transactional
    public CandidateRegistration updateStatus(
            Integer driveId,
            UUID registrationReference,
            CandidateRegistrationStatus targetStatus,
            String changedBy) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        String actor = normalizeBlank(changedBy);
        if (actor == null || actor.length() > 100) {
            throw new IllegalArgumentException("status change requires a valid authenticated user");
        }

        CandidateRegistration registration = getRegistration(driveId, registrationReference);
        CandidateRegistrationStatus currentStatus = registration.getStatus();
        if (currentStatus == targetStatus) {
            return registration;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new ResourceConflictException(
                    "Candidate status cannot change from " + currentStatus + " to " + targetStatus);
        }

        registration.setStatus(targetStatus);
        registration.setStatusChangedAt(now());
        registration.setStatusChangedBy(actor);
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

    private HiringDrive requireDrive(Integer driveId) {
        if (driveId == null || driveId < 1) {
            throw new IllegalArgumentException("driveId must be positive");
        }
        return driveService.getDriveById(driveId);
    }

    private String normalizeSearch(String query) {
        String normalized = normalizeBlank(query);
        if (normalized != null && normalized.length() > MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException("query must be 100 characters or fewer");
        }
        return normalized;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
