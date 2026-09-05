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
import com.walkin.service.CandidateRoundScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CandidateRoundScheduleServiceImpl implements CandidateRoundScheduleService {

    private static final Map<ScheduleStatus, Set<ScheduleStatus>> ALLOWED_TRANSITIONS = Map.of(
            ScheduleStatus.SCHEDULED, Set.of(
                    ScheduleStatus.NOTIFIED,
                    ScheduleStatus.REPORTED,
                    ScheduleStatus.MISSED,
                    ScheduleStatus.CANCELLED),
            ScheduleStatus.NOTIFIED, Set.of(
                    ScheduleStatus.REPORTED,
                    ScheduleStatus.MISSED,
                    ScheduleStatus.CANCELLED),
            ScheduleStatus.REPORTED, Set.of(),
            ScheduleStatus.MISSED, Set.of(),
            ScheduleStatus.CANCELLED, Set.of());

    private final CandidateRoundScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final CompanyCustomRoundRepository companyRoundRepository;
    private final Clock clock;

    public CandidateRoundScheduleServiceImpl(
            CandidateRoundScheduleRepository scheduleRepository,
            StudentRepository studentRepository,
            CompanyCustomRoundRepository companyRoundRepository,
            Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.studentRepository = studentRepository;
        this.companyRoundRepository = companyRoundRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CandidateRoundSchedule createSchedule(CandidateRoundScheduleRequest request) {
        requireFutureReportingTime(request.reportingTime());
        if (scheduleRepository.existsByStudent_StudentIdAndCompanyRound_CompanyRoundId(
                request.studentId(), request.companyRoundId())) {
            throw new ResourceConflictException(
                    "A schedule already exists for this candidate and company round");
        }

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID: " + request.studentId()));
        CompanyCustomRound companyRound = companyRoundRepository.findById(request.companyRoundId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company round not found with ID: " + request.companyRoundId()));

        CandidateRoundSchedule schedule = new CandidateRoundSchedule();
        schedule.setStudent(student);
        schedule.setCompanyRound(companyRound);
        schedule.setReportingTime(request.reportingTime());
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        return scheduleRepository.save(schedule);
    }

    @Override
    public CandidateRoundSchedule getScheduleById(Integer scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate round schedule not found with ID: " + scheduleId));
    }

    @Override
    public Page<CandidateRoundSchedule> getSchedules(Pageable pageable) {
        return scheduleRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public CandidateRoundSchedule reschedule(Integer scheduleId, OffsetDateTime reportingTime) {
        requireFutureReportingTime(reportingTime);
        CandidateRoundSchedule schedule = getScheduleById(scheduleId);
        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new ResourceConflictException(
                    "Only a SCHEDULED candidate round can be rescheduled");
        }
        schedule.setReportingTime(reportingTime);
        return scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public CandidateRoundSchedule updateStatus(Integer scheduleId, ScheduleStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        CandidateRoundSchedule schedule = getScheduleById(scheduleId);
        ScheduleStatus currentStatus = schedule.getStatus();
        if (currentStatus == targetStatus) {
            return schedule;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new ResourceConflictException(
                    "Schedule status cannot change from " + currentStatus + " to " + targetStatus);
        }

        OffsetDateTime eventTime = now();
        schedule.setStatus(targetStatus);
        if (targetStatus == ScheduleStatus.NOTIFIED) {
            schedule.setNotifiedAt(eventTime);
        } else if (targetStatus == ScheduleStatus.REPORTED) {
            schedule.setReportedAt(eventTime);
        }
        return scheduleRepository.save(schedule);
    }

    private void requireFutureReportingTime(OffsetDateTime reportingTime) {
        if (reportingTime == null || !reportingTime.isAfter(now())) {
            throw new IllegalArgumentException("reportingTime must be in the future");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
