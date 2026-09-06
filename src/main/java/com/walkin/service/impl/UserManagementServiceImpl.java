package com.walkin.service.impl;

import com.walkin.dto.*;
import com.walkin.entity.ApplicationUser;
import com.walkin.entity.ApplicationUser.Role;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.ApplicationUserRepository;
import com.walkin.repository.CompanyRepository;
import com.walkin.service.UserManagementService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {
    private final ApplicationUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companies;

    public UserManagementServiceImpl(
            ApplicationUserRepository users,
            PasswordEncoder passwordEncoder,
            CompanyRepository companies) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.companies = companies;
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ResourceConflictException("Username is already in use");
        }
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        assignCompany(user, request.role(), request.companyId());
        return UserResponse.from(users.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.from(users.findAll(pageable).map(UserResponse::from));
    }

    @Override
    public UserResponse update(Integer id, UpdateUserRequest request) {
        ApplicationUser user = find(id);
        if (user.getRole() == Role.PLATFORM_ADMIN && user.isEnabled()
                && (request.role() != Role.PLATFORM_ADMIN || !request.enabled())
                && users.countByRoleAndEnabledTrue(Role.PLATFORM_ADMIN) <= 1) {
            throw new ResourceConflictException(
                    "The last enabled platform administrator cannot be disabled or demoted");
        }
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        assignCompany(user, request.role(), request.companyId());
        return UserResponse.from(users.save(user));
    }

    @Override
    public void resetPassword(Integer id, ResetPasswordRequest request) {
        ApplicationUser user = find(id);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.save(user);
    }

    private ApplicationUser find(Integer id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void assignCompany(ApplicationUser user, Role role, Integer companyId) {
        if (role == Role.PLATFORM_ADMIN) {
            if (companyId != null) {
                throw new IllegalArgumentException(
                        "A platform administrator cannot belong to a company");
            }
            user.setCompany(null);
            return;
        }
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required for company users");
        }
        user.setCompany(companies.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ID: " + companyId)));
    }
}
