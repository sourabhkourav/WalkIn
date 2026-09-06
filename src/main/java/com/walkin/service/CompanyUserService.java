package com.walkin.service;

import com.walkin.dto.CreateRecruiterRequest;
import com.walkin.dto.PageResponse;
import com.walkin.dto.UserResponse;
import org.springframework.data.domain.Pageable;

public interface CompanyUserService {
    UserResponse createRecruiter(Integer companyId, CreateRecruiterRequest request);
    PageResponse<UserResponse> getUsers(Integer companyId, Pageable pageable);
    UserResponse updateRecruiterStatus(Integer companyId, Integer userId, boolean enabled);
}
