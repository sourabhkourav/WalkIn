package com.walkin.service.impl;

import com.walkin.dto.CandidateRoundScheduleRequest;
import com.walkin.entity.CandidateRoundSchedule;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.ScheduleStatus;
import com.walkin.entity.Student;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CandidateRoundScheduleRepository;
import com.walkin.repository.CompanyCustomRoundRepository;
import com.walkin.repository.StudentRepository;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CandidateRoundScheduleServiceImplTests {

    private static final Instant NOW = Instant.parse("2026-09-05T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private CandidateRoundScheduleRepository scheduleRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CompanyCustomRoundRepository companyRoundRepository;

    private CandidateRoundScheduleServiceImpl scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new CandidateRoundScheduleServiceImpl(
                scheduleRepository, studentRepository, companyRoundRepository, FIXED_CLOCK);
    }

    @Test
    void createsScheduledEntryForExistingStudentAndCompanyRound() {
        OffsetDateTime reportingTime = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(2);
        CandidateRoundScheduleRequest request = new CandidateRoundScheduleRequest(1, 2, reportingTime);
        Student student = new Student();
        CompanyCustomRound companyRound = new CompanyCustomRound();
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(companyRoundRepository.findById(2)).thenReturn(Optional.of(companyRound));
        when(scheduleRepository.save(any(CandidateRoundSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CandidateRoundSchedule result = scheduleService.createSchedule(request);

        ArgumentCaptor<CandidateRoundSchedule> captor =
                ArgumentCaptor.forClass(CandidateRoundSchedule.class);
        verify(scheduleRepository).save(captor.capture());
        assertThat(result).isSameAs(captor.getValue());
        assertThat(result.getStudent()).isSameAs(student);
        assertThat(result.getCompanyRound()).isSameAs(companyRound);
        assertThat(result.getReportingTime()).isEqualTo(reportingTime);
        assertThat(result.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(result.getNotifiedAt()).isNull();
        assertThat(result.getReportedAt()).isNull();
    }

    @Test
    void rejectsDuplicateCandidateAndCompanyRoundSchedule() {
        CandidateRoundScheduleRequest request = request();
        when(scheduleRepository.existsByStudent_StudentIdAndCompanyRound_CompanyRoundId(1, 2))
                .thenReturn(true);

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("A schedule already exists for this candidate and company round");

        verify(scheduleRepository, never()).save(any());
        verifyNoInteractions(studentRepository, companyRoundRepository);
    }

    @Test
    void rejectsUnknownStudentBeforeLoadingCompanyRound() {
        CandidateRoundScheduleRequest request = request();
        when(studentRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Student not found with ID: 1");

        verifyNoInteractions(companyRoundRepository);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownCompanyRound() {
        CandidateRoundScheduleRequest request = request();
        when(studentRepository.findById(1)).thenReturn(Optional.of(new Student()));
        when(companyRoundRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.createSchedule(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company round not found with ID: 2");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void getsScheduleByIdOrReportsMissingRecord() {
        CandidateRoundSchedule schedule = new CandidateRoundSchedule();
        when(scheduleRepository.findById(7)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.findById(8)).thenReturn(Optional.empty());

        assertThat(scheduleService.getScheduleById(7)).isSameAs(schedule);
        assertThatThrownBy(() -> scheduleService.getScheduleById(8))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Candidate round schedule not found with ID: 8");
    }

    @Test
    void returnsRequestedPageOfSchedules() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<CandidateRoundSchedule> page = new PageImpl<>(List.of(new CandidateRoundSchedule()));
        when(scheduleRepository.findAll(pageable)).thenReturn(page);

        assertThat(scheduleService.getSchedules(pageable)).isSameAs(page);
    }

    @Test
    void reschedulesOnlyScheduledEntries() {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.SCHEDULED);
        OffsetDateTime newTime = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(3);
        when(scheduleRepository.findById(10)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        assertThat(scheduleService.reschedule(10, newTime)).isSameAs(schedule);
        assertThat(schedule.getReportingTime()).isEqualTo(newTime);
        verify(scheduleRepository).save(schedule);
    }

    @Test
    void rejectsReschedulingAfterNotification() {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.NOTIFIED);
        when(scheduleRepository.findById(10)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.reschedule(
                10, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(3)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Only a SCHEDULED candidate round can be rescheduled");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void rejectsPastReportingTimeInsideServiceBoundary() {
        assertThatThrownBy(() -> scheduleService.reschedule(
                10, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reportingTime must be in the future");

        verifyNoInteractions(scheduleRepository);
    }

    @Test
    void recordsNotificationAndReportingTimesDuringTransitions() {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.SCHEDULED);
        when(scheduleRepository.findById(10)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        scheduleService.updateStatus(10, ScheduleStatus.NOTIFIED);

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.NOTIFIED);
        assertThat(schedule.getNotifiedAt()).isEqualTo(
                OffsetDateTime.parse("2026-09-05T10:00:00Z"));

        scheduleService.updateStatus(10, ScheduleStatus.REPORTED);

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.REPORTED);
        assertThat(schedule.getReportedAt()).isEqualTo(
                OffsetDateTime.parse("2026-09-05T10:00:00Z"));
        verify(scheduleRepository, times(2)).save(schedule);
    }

    @Test
    void rejectsTransitionFromTerminalStatus() {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.REPORTED);
        when(scheduleRepository.findById(10)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.updateStatus(10, ScheduleStatus.CANCELLED))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Schedule status cannot change from REPORTED to CANCELLED");

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void repeatedStatusUpdateIsIdempotent() {
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.NOTIFIED);
        when(scheduleRepository.findById(10)).thenReturn(Optional.of(schedule));

        assertThat(scheduleService.updateStatus(10, ScheduleStatus.NOTIFIED)).isSameAs(schedule);

        verify(scheduleRepository).findById(10);
        verifyNoMoreInteractions(scheduleRepository);
    }

    @Test
    void rejectsMissingTargetStatusInsideServiceBoundary() {
        assertThatThrownBy(() -> scheduleService.updateStatus(10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("status is required");

        verifyNoInteractions(scheduleRepository);
    }

    @Test
    void returnsOnlySchedulesInsideTheirCandidateNoticeWindow() {
        CandidateRoundSchedule dueAtBoundary = schedule(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(30), 30);
        CandidateRoundSchedule notDueYet = schedule(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(31), 30);
        CandidateRoundSchedule dueWithLongerNotice = schedule(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(120), 120);
        CandidateRoundSchedule reportingTimePassed = schedule(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).minusMinutes(1), 30);
        when(scheduleRepository
                .findByStatusAndReportingTimeBetweenOrderByReportingTimeAsc(
                        ScheduleStatus.SCHEDULED,
                        OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(240)))
                .thenReturn(List.of(
                        reportingTimePassed, dueAtBoundary, notDueYet, dueWithLongerNotice));

        assertThat(scheduleService.getDueNotificationSchedules())
                .containsExactly(dueAtBoundary, dueWithLongerNotice);
    }

    @Test
    void limitsDueNotificationBatchAfterEvaluatingNoticeWindows() {
        CandidateRoundSchedule due = schedule(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(30), 30);
        when(scheduleRepository
                .findByStatusAndReportingTimeBetweenOrderByReportingTimeAsc(
                        ScheduleStatus.SCHEDULED,
                        OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusMinutes(240)))
                .thenReturn(java.util.Collections.nCopies(101, due));

        assertThat(scheduleService.getDueNotificationSchedules()).hasSize(100);
    }

    private CandidateRoundScheduleRequest request() {
        return new CandidateRoundScheduleRequest(
                1, 2, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).plusHours(1));
    }

    private CandidateRoundSchedule schedule(ScheduleStatus status) {
        CandidateRoundSchedule schedule = new CandidateRoundSchedule();
        schedule.setStatus(status);
        return schedule;
    }

    private CandidateRoundSchedule schedule(
            OffsetDateTime reportingTime,
            int advanceNoticeMinutes) {
        Student student = new Student();
        student.setAdvanceNoticeMinutes(advanceNoticeMinutes);
        CandidateRoundSchedule schedule = schedule(ScheduleStatus.SCHEDULED);
        schedule.setStudent(student);
        schedule.setReportingTime(reportingTime);
        return schedule;
    }
}
