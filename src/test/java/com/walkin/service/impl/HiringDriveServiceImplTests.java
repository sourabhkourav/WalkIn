package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRequest;
import com.walkin.dto.HiringDriveRegistrationFormRequest;
import com.walkin.entity.Company;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyRepository;
import com.walkin.repository.HiringDriveRepository;
import com.walkin.security.RegistrationTokenService;
import com.walkin.service.HiringDriveCreation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HiringDriveServiceImplTests {

    private static final Instant NOW = Instant.parse("2026-09-05T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private HiringDriveRepository driveRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RegistrationTokenService tokenService;

    private HiringDriveServiceImpl driveService;

    @BeforeEach
    void setUp() {
        driveService = new HiringDriveServiceImpl(
                driveRepository, companyRepository, tokenService, FIXED_CLOCK);
    }

    @Test
    void createsDraftDriveAndReturnsRawTokenOnlyInCreationResult() {
        Company company = new Company();
        HiringDriveRequest request = request();
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(tokenService.generateToken()).thenReturn("raw-registration-token");
        when(tokenService.hashToken("raw-registration-token")).thenReturn("hashed-token");
        when(driveRepository.save(any(HiringDrive.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HiringDriveCreation creation = driveService.createDrive(request);

        ArgumentCaptor<HiringDrive> captor = ArgumentCaptor.forClass(HiringDrive.class);
        verify(driveRepository).save(captor.capture());
        HiringDrive saved = captor.getValue();
        assertThat(creation.hiringDrive()).isSameAs(saved);
        assertThat(creation.registrationToken()).isEqualTo("raw-registration-token");
        assertThat(saved.getRegistrationTokenHash()).isEqualTo("hashed-token");
        assertThat(saved.getRegistrationTokenHash()).isNotEqualTo(creation.registrationToken());
        assertThat(saved.getCompany()).isSameAs(company);
        assertThat(saved.getDriveName()).isEqualTo("Engineering Drive");
        assertThat(saved.getVenue()).isEqualTo("Convention Centre");
        assertThat(saved.getStatus()).isEqualTo(HiringDriveStatus.DRAFT);
        assertThat(saved.getTokenExpiresAt()).isEqualTo(request.endsAt());
        assertThat(saved.getFirstNameRequirement())
                .isEqualTo(RegistrationFieldRequirement.REQUIRED);
        assertThat(saved.getResumeRequirement())
                .isEqualTo(RegistrationFieldRequirement.HIDDEN);
    }

    @Test
    void rejectsInvalidTimeRangeBeforeLoadingCompany() {
        HiringDriveRequest request = new HiringDriveRequest(
                1,
                "Drive",
                "Venue",
                time(4),
                time(2));

        assertThatThrownBy(() -> driveService.createDrive(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startsAt must be before endsAt");

        verifyNoInteractions(companyRepository, driveRepository, tokenService);
    }

    @Test
    void rejectsDriveThatHasAlreadyEnded() {
        HiringDriveRequest request = new HiringDriveRequest(
                1,
                "Drive",
                "Venue",
                time(-2),
                time(-1));

        assertThatThrownBy(() -> driveService.createDrive(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endsAt must be in the future");
    }

    @Test
    void rejectsUnknownCompanyBeforeGeneratingToken() {
        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driveService.createDrive(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company not found with ID: 1");

        verifyNoInteractions(tokenService);
        verify(driveRepository, never()).save(any());
    }

    @Test
    void regeneratesTokenWhenHashAlreadyExists() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(new Company()));
        when(tokenService.generateToken()).thenReturn("first-token", "second-token");
        when(tokenService.hashToken("first-token")).thenReturn("existing-hash");
        when(tokenService.hashToken("second-token")).thenReturn("unique-hash");
        when(driveRepository.existsByRegistrationTokenHash("existing-hash")).thenReturn(true);
        when(driveRepository.save(any(HiringDrive.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HiringDriveCreation creation = driveService.createDrive(request());

        assertThat(creation.registrationToken()).isEqualTo("second-token");
        assertThat(creation.hiringDrive().getRegistrationTokenHash()).isEqualTo("unique-hash");
    }

    @Test
    void opensActiveDraftAndSupportsIdempotentRepeat() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, time(8));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(driveRepository.save(drive)).thenReturn(drive);

        assertThat(driveService.updateStatus(10, HiringDriveStatus.OPEN)).isSameAs(drive);
        assertThat(drive.getStatus()).isEqualTo(HiringDriveStatus.OPEN);
        verify(driveRepository).save(drive);

        assertThat(driveService.updateStatus(10, HiringDriveStatus.OPEN)).isSameAs(drive);
    }

    @Test
    void rejectsOpeningDriveThatHasEnded() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, time(-1));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));

        assertThatThrownBy(() -> driveService.updateStatus(10, HiringDriveStatus.OPEN))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("An ended hiring drive cannot be opened");

        verify(driveRepository, never()).save(any());
    }

    @Test
    void rejectsTransitionFromTerminalStatus() {
        HiringDrive drive = drive(HiringDriveStatus.CLOSED, time(8));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));

        assertThatThrownBy(() -> driveService.updateStatus(10, HiringDriveStatus.OPEN))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Hiring drive status cannot change from CLOSED to OPEN");
    }

    @Test
    void returnsOnlyOpenUnexpiredDriveForPublicToken() {
        HiringDrive drive = drive(HiringDriveStatus.OPEN, time(8));
        drive.setTokenExpiresAt(time(8));
        when(tokenService.hashToken("public-token")).thenReturn("token-hash");
        when(driveRepository.findByRegistrationTokenHash("token-hash"))
                .thenReturn(Optional.of(drive));

        assertThat(driveService.getOpenDriveByRegistrationToken("public-token")).isSameAs(drive);
    }

    @Test
    void hidesWhetherPublicTokenIsInvalidClosedOrExpired() {
        when(tokenService.hashToken("missing-token")).thenReturn("missing-hash");
        when(driveRepository.findByRegistrationTokenHash("missing-hash"))
                .thenReturn(Optional.empty());
        HiringDrive closed = drive(HiringDriveStatus.CLOSED, time(8));
        closed.setTokenExpiresAt(time(8));
        when(tokenService.hashToken("closed-token")).thenReturn("closed-hash");
        when(driveRepository.findByRegistrationTokenHash("closed-hash"))
                .thenReturn(Optional.of(closed));
        HiringDrive expired = drive(HiringDriveStatus.OPEN, time(8));
        expired.setTokenExpiresAt(time(-1));
        when(tokenService.hashToken("expired-token")).thenReturn("expired-hash");
        when(driveRepository.findByRegistrationTokenHash("expired-hash"))
                .thenReturn(Optional.of(expired));

        assertUnavailable("missing-token");
        assertUnavailable("closed-token");
        assertUnavailable("expired-token");
    }

    @Test
    void rejectsBlankPublicTokenWithoutHashingIt() {
        assertUnavailable(" ");
        verifyNoInteractions(tokenService, driveRepository);
    }

    @Test
    void returnsRequestedPageOfDrives() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<HiringDrive> page = new PageImpl<>(List.of(new HiringDrive()));
        when(driveRepository.findAll(pageable)).thenReturn(page);

        assertThat(driveService.getDrives(pageable)).isSameAs(page);
    }

    @Test
    void updatesRegistrationFormWhileDriveIsDraft() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, time(8));
        HiringDriveRegistrationFormRequest request = registrationForm();
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(driveRepository.save(drive)).thenReturn(drive);

        assertThat(driveService.updateRegistrationForm(10, request)).isSameAs(drive);
        assertThat(drive.getFirstNameRequirement())
                .isEqualTo(RegistrationFieldRequirement.REQUIRED);
        assertThat(drive.getLastNameRequirement())
                .isEqualTo(RegistrationFieldRequirement.OPTIONAL);
        assertThat(drive.getEmailRequirement())
                .isEqualTo(RegistrationFieldRequirement.REQUIRED);
        assertThat(drive.getContactNumberRequirement())
                .isEqualTo(RegistrationFieldRequirement.HIDDEN);
        assertThat(drive.getResumeRequirement())
                .isEqualTo(RegistrationFieldRequirement.OPTIONAL);
    }

    @Test
    void rejectsRegistrationFormChangesAfterDriveOpens() {
        HiringDrive drive = drive(HiringDriveStatus.OPEN, time(8));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));

        assertThatThrownBy(() -> driveService.updateRegistrationForm(10, registrationForm()))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage(
                        "Registration fields can be changed only while the hiring drive is DRAFT");

        verify(driveRepository, never()).save(any());
    }

    @Test
    void rejectsRegistrationFormWithoutVisibleFirstName() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, time(8));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        HiringDriveRegistrationFormRequest request = new HiringDriveRegistrationFormRequest(
                RegistrationFieldRequirement.HIDDEN,
                RegistrationFieldRequirement.OPTIONAL,
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.HIDDEN,
                RegistrationFieldRequirement.OPTIONAL);

        assertThatThrownBy(() -> driveService.updateRegistrationForm(10, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("firstName cannot be hidden");
    }

    @Test
    void rejectsRegistrationFormWithoutContactMethod() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, time(8));
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        HiringDriveRegistrationFormRequest request = new HiringDriveRegistrationFormRequest(
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.OPTIONAL,
                RegistrationFieldRequirement.HIDDEN,
                RegistrationFieldRequirement.HIDDEN,
                RegistrationFieldRequirement.OPTIONAL);

        assertThatThrownBy(() -> driveService.updateRegistrationForm(10, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one of email or contactNumber must be visible");
    }

    private void assertUnavailable(String token) {
        assertThatThrownBy(() -> driveService.getOpenDriveByRegistrationToken(token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hiring drive is unavailable");
    }

    private HiringDriveRequest request() {
        return new HiringDriveRequest(
                1,
                "  Engineering Drive  ",
                "  Convention Centre  ",
                time(1),
                time(8));
    }

    private HiringDriveRegistrationFormRequest registrationForm() {
        return new HiringDriveRegistrationFormRequest(
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.OPTIONAL,
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.HIDDEN,
                RegistrationFieldRequirement.OPTIONAL);
    }

    private HiringDrive drive(HiringDriveStatus status, OffsetDateTime endsAt) {
        HiringDrive drive = new HiringDrive();
        drive.setStatus(status);
        drive.setEndsAt(endsAt);
        return drive;
    }

    private OffsetDateTime time(long hoursFromNow) {
        return OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(hoursFromNow);
    }
}
