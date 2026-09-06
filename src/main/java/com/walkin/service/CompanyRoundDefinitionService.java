package com.walkin.service;

import com.walkin.dto.CompanyRoundDefinitionRequest;
import com.walkin.entity.CompanyCustomRound;

public interface CompanyRoundDefinitionService {
    CompanyCustomRound create(CompanyRoundDefinitionRequest request);
}
