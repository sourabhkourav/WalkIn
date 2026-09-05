package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRoundRequest;
import com.walkin.entity.Company;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyCustomRoundRepository;
import com.walkin.repository.HiringDriveRepository;
import com.walkin.repository.HiringDriveRoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class HiringDriveRoundServiceImplTests {

    @Mock
    private HiringDriveRoundRepository driveRoundRepository;

    @Mock
    private HiringDriveRepository driveRepository;

    @Mock
    private CompanyCustomRoundRepository companyRoundRepository;

    private HiringDriveRoundServiceImpl driveRoundService;

    @BeforeEach
    void setUp() {
        driveRoundService = new HiringDriveRoundServiceImpl(
                driveRoundRepository, driveRepository, companyRoundRepository);
    }

    @Test
    void addsCompanyRoundToDraftDriveAtRequestedPosition() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, 1);
        CompanyCustomRound companyRound = companyRound(1);
        HiringDriveRoundRequest request = new HiringDriveRoundRequest(20, 2);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(companyRoundRepository.findById(20)).thenReturn(Optional.of(companyRound));
        when(driveRoundRepository.save(any(HiringDriveRound.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HiringDriveRound result = driveRoundService.addRound(10, request);

        ArgumentCaptor<HiringDriveRound> captor = ArgumentCaptor.forClass(HiringDriveRound.class);
        verify(driveRoundRepository).save(captor.capture());
        assertThat(result).isSameAs(captor.getValue());
        assertThat(result.getHiringDrive()).isSameAs(drive);
        assertThat(result.getCompanyRound()).isSameAs(companyRound);
        assertThat(result.getRoundOrder()).isEqualTo(2);
    }

    @Test
    void allowsRoundToBeAddedWhileDriveIsOpen() {
        HiringDrive drive = drive(HiringDriveStatus.OPEN, 1);
        CompanyCustomRound companyRound = companyRound(1);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(companyRoundRepository.findById(20)).thenReturn(Optional.of(companyRound));
        when(driveRoundRepository.save(any(HiringDriveRound.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HiringDriveRound result = driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1));

        assertThat(result.getRoundOrder()).isEqualTo(1);
        verify(driveRoundRepository).save(result);
    }

    @Test
    void rejectsRoundForTerminalDrive() {
        HiringDrive drive = drive(HiringDriveStatus.CLOSED);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Rounds can be added only to DRAFT or OPEN hiring drives");

        verifyNoInteractions(companyRoundRepository, driveRoundRepository);
    }

    @Test
    void rejectsUnknownDrive() {
        when(driveRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hiring drive not found with ID: 10");

        verifyNoInteractions(companyRoundRepository, driveRoundRepository);
    }

    @Test
    void rejectsUnknownCompanyRound() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(companyRoundRepository.findById(20)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company round not found with ID: 20");

        verify(driveRoundRepository, never()).save(any());
    }

    @Test
    void rejectsRoundOwnedByDifferentCompany() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, 1);
        CompanyCustomRound companyRound = companyRound(2);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(companyRoundRepository.findById(20)).thenReturn(Optional.of(companyRound));

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("The company round belongs to a different company");

        verify(driveRoundRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateRoundAssignment() {
        stubMatchingDriveAndRound();
        when(driveRoundRepository
                .existsByHiringDrive_DriveIdAndCompanyRound_CompanyRoundId(10, 20))
                .thenReturn(true);

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("This company round is already assigned to the hiring drive");

        verify(driveRoundRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateRoundOrder() {
        stubMatchingDriveAndRound();
        when(driveRoundRepository.existsByHiringDrive_DriveIdAndRoundOrder(10, 1))
                .thenReturn(true);

        assertThatThrownBy(() -> driveRoundService.addRound(
                10, new HiringDriveRoundRequest(20, 1)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Another round already uses order 1");

        verify(driveRoundRepository, never()).save(any());
    }

    @Test
    void returnsRoundsInRepositoryOrderAfterValidatingDrive() {
        HiringDriveRound first = new HiringDriveRound();
        HiringDriveRound second = new HiringDriveRound();
        List<HiringDriveRound> rounds = List.of(first, second);
        HiringDrive drive = drive(HiringDriveStatus.OPEN);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(driveRoundRepository.findByHiringDrive_DriveIdOrderByRoundOrderAsc(10))
                .thenReturn(rounds);

        assertThat(driveRoundService.getRounds(10)).containsExactly(first, second);
    }

    private void stubMatchingDriveAndRound() {
        HiringDrive drive = drive(HiringDriveStatus.DRAFT, 1);
        CompanyCustomRound companyRound = companyRound(1);
        when(driveRepository.findById(10)).thenReturn(Optional.of(drive));
        when(companyRoundRepository.findById(20)).thenReturn(Optional.of(companyRound));
    }

    private HiringDrive drive(HiringDriveStatus status, int companyId) {
        HiringDrive drive = drive(status);
        drive.setCompany(company(companyId));
        return drive;
    }

    private HiringDrive drive(HiringDriveStatus status) {
        HiringDrive drive = new HiringDrive();
        drive.setStatus(status);
        return drive;
    }

    private CompanyCustomRound companyRound(int companyId) {
        CompanyCustomRound companyRound = new CompanyCustomRound();
        companyRound.setCompany(company(companyId));
        return companyRound;
    }

    private Company company(int companyId) {
        Company company = org.mockito.Mockito.mock(Company.class);
        when(company.getCompanyId()).thenReturn(companyId);
        return company;
    }
}
