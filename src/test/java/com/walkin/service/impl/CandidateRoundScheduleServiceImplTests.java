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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
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
class CandidateRoundScheduleServiceImplTests {

    @Mock
    private CandidateRoundScheduleRepository scheduleRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CompanyCustomRoundRepository companyRoundRepository;

    @InjectMocks
    private CandidateRoundScheduleServiceImpl scheduleService;

    @Test
    void createsScheduledEntryForExistingStudentAndCompanyRound() {
        OffsetDateTime reportingTime = OffsetDateTime.now().plusHours(2);
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

    private CandidateRoundScheduleRequest request() {
        return new CandidateRoundScheduleRequest(1, 2, OffsetDateTime.now().plusHours(1));
    }
}
