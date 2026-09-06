package com.walkin.service.impl;

import com.walkin.dto.HiringDriveRegistrationFormRequest;
import com.walkin.dto.HiringDriveSetupRequest;
import com.walkin.entity.HiringDrive;
import com.walkin.entity.HiringDriveRound;
import com.walkin.entity.HiringDriveStatus;
import com.walkin.entity.RegistrationFieldRequirement;
import com.walkin.service.HiringDriveCreation;
import com.walkin.service.HiringDriveRoundService;
import com.walkin.service.HiringDriveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HiringDriveSetupServiceImplTests {

    @Mock HiringDriveService driveService;
    @Mock HiringDriveRoundService roundService;

    @Test
    void createsFormOrderedRoundsAndOpensDriveAsOneWorkflow() {
        HiringDrive drive = org.mockito.Mockito.mock(HiringDrive.class);
        HiringDriveRound first = org.mockito.Mockito.mock(HiringDriveRound.class);
        HiringDriveRound second = org.mockito.Mockito.mock(HiringDriveRound.class);
        when(drive.getDriveId()).thenReturn(42);
        when(driveService.createDrive(any()))
                .thenReturn(new HiringDriveCreation(drive, "raw-token"));
        when(driveService.updateRegistrationForm(42, form())).thenReturn(drive);
        when(roundService.addRound(42, new com.walkin.dto.HiringDriveRoundRequest(8, 1)))
                .thenReturn(first);
        when(roundService.addRound(42, new com.walkin.dto.HiringDriveRoundRequest(5, 2)))
                .thenReturn(second);
        when(driveService.updateStatus(42, HiringDriveStatus.OPEN)).thenReturn(drive);

        var result = service().create(request(List.of(8, 5), true));

        assertEquals("raw-token", result.registrationToken());
        assertEquals(List.of(first, second), result.rounds());
        InOrder order = inOrder(driveService, roundService);
        order.verify(driveService).createDrive(any());
        order.verify(driveService).updateRegistrationForm(42, form());
        order.verify(roundService).addRound(
                42, new com.walkin.dto.HiringDriveRoundRequest(8, 1));
        order.verify(roundService).addRound(
                42, new com.walkin.dto.HiringDriveRoundRequest(5, 2));
        order.verify(driveService).updateStatus(42, HiringDriveStatus.OPEN);
    }

    @Test
    void rejectsDuplicateRoundsBeforeCreatingAnything() {
        assertThrows(IllegalArgumentException.class,
                () -> service().create(request(List.of(8, 8), false)));
        verify(driveService, never()).createDrive(any());
    }

    private HiringDriveSetupServiceImpl service() {
        return new HiringDriveSetupServiceImpl(driveService, roundService);
    }

    private HiringDriveSetupRequest request(List<Integer> rounds, boolean open) {
        return new HiringDriveSetupRequest(
                7, "Engineering Drive", "Hall A",
                OffsetDateTime.parse("2030-01-01T09:00:00Z"),
                OffsetDateTime.parse("2030-01-01T17:00:00Z"),
                form(), rounds, open);
    }

    private HiringDriveRegistrationFormRequest form() {
        return new HiringDriveRegistrationFormRequest(
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.REQUIRED,
                RegistrationFieldRequirement.OPTIONAL,
                RegistrationFieldRequirement.OPTIONAL);
    }
}
