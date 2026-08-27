package com.walkin.service.impl;

import com.walkin.dto.*;
import com.walkin.entity.ApplicationUser;
import com.walkin.entity.ApplicationUser.Role;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.ApplicationUserRepository;
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

    public UserManagementServiceImpl(ApplicationUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
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
        if (user.getRole() == Role.ADMIN && user.isEnabled()
                && (request.role() != Role.ADMIN || !request.enabled())
                && users.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
            throw new ResourceConflictException("The last enabled administrator cannot be disabled or demoted");
        }
        user.setRole(request.role());
        user.setEnabled(request.enabled());
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
}
