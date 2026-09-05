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

@Service
@Transactional(readOnly = true)
public class CandidateRoundScheduleServiceImpl implements CandidateRoundScheduleService {

    private final CandidateRoundScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final CompanyCustomRoundRepository companyRoundRepository;

    public CandidateRoundScheduleServiceImpl(
            CandidateRoundScheduleRepository scheduleRepository,
            StudentRepository studentRepository,
            CompanyCustomRoundRepository companyRoundRepository) {
        this.scheduleRepository = scheduleRepository;
        this.studentRepository = studentRepository;
        this.companyRoundRepository = companyRoundRepository;
    }

    @Override
    @Transactional
    public CandidateRoundSchedule createSchedule(CandidateRoundScheduleRequest request) {
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
}
