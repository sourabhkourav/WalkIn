package com.walkin.service.impl;

import com.walkin.dto.CreateRecruiterRequest;
import com.walkin.dto.PageResponse;
import com.walkin.dto.UserResponse;
import com.walkin.entity.ApplicationUser;
import com.walkin.exception.ResourceConflictException;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.ApplicationUserRepository;
import com.walkin.repository.CompanyRepository;
import com.walkin.service.CompanyUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyUserServiceImpl implements CompanyUserService {

    private final ApplicationUserRepository users;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;

    public CompanyUserServiceImpl(
            ApplicationUserRepository users,
            CompanyRepository companies,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createRecruiter(Integer companyId, CreateRecruiterRequest request) {
        String username = request.username().trim();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ResourceConflictException("Username is already in use");
        }
        ApplicationUser recruiter = new ApplicationUser();
        recruiter.setUsername(username);
        recruiter.setPasswordHash(passwordEncoder.encode(request.password()));
        recruiter.setRole(ApplicationUser.Role.RECRUITER);
        recruiter.setCompany(companies.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ID: " + companyId)));
        return UserResponse.from(users.save(recruiter));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Integer companyId, Pageable pageable) {
        return PageResponse.from(users.findByCompany_CompanyId(companyId, pageable)
                .map(UserResponse::from));
    }

    @Override
    public UserResponse updateRecruiterStatus(
            Integer companyId, Integer userId, boolean enabled) {
        ApplicationUser recruiter = users.findById(userId)
                .filter(user -> user.getCompany() != null
                        && user.getCompany().getCompanyId().equals(companyId)
                        && user.getRole() == ApplicationUser.Role.RECRUITER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recruiter not found with ID: " + userId));
        recruiter.setEnabled(enabled);
        return UserResponse.from(users.save(recruiter));
    }
}
