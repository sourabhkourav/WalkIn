package com.walkin.service.impl;

import com.walkin.dto.CompanyRoundDefinitionRequest;
import com.walkin.entity.CompanyCustomRound;
import com.walkin.entity.InterviewRound;
import com.walkin.service.CompanyCustomRoundService;
import com.walkin.service.CompanyRoundDefinitionService;
import com.walkin.service.CompanyService;
import com.walkin.service.InterviewRoundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyRoundDefinitionServiceImpl implements CompanyRoundDefinitionService {

    private final CompanyService companyService;
    private final InterviewRoundService interviewRoundService;
    private final CompanyCustomRoundService companyRoundService;

    public CompanyRoundDefinitionServiceImpl(
            CompanyService companyService,
            InterviewRoundService interviewRoundService,
            CompanyCustomRoundService companyRoundService) {
        this.companyService = companyService;
        this.interviewRoundService = interviewRoundService;
        this.companyRoundService = companyRoundService;
    }

    @Override
    @Transactional
    public CompanyCustomRound create(CompanyRoundDefinitionRequest request) {
        InterviewRound definition = new InterviewRound();
        definition.setRoundName(request.roundName().trim());
        definition.setDescription(request.description().trim());

        CompanyCustomRound companyRound = new CompanyCustomRound();
        companyRound.setCompany(companyService.getCompanyById(request.companyId()));
        companyRound.setInterviewRound(
                interviewRoundService.createInterviewRound(definition));
        return companyRoundService.createCompanyCustomRound(companyRound);
    }
}
