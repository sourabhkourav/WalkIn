package com.walkin.service;

import com.walkin.dto.*;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {
    UserResponse create(CreateUserRequest request);
    PageResponse<UserResponse> getAll(Pageable pageable);
    UserResponse update(Integer id, UpdateUserRequest request);
    void resetPassword(Integer id, ResetPasswordRequest request);
}
