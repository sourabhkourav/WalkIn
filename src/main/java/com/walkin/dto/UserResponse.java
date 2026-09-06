package com.walkin.dto;

import com.walkin.entity.ApplicationUser;
import com.walkin.entity.ApplicationUser.Role;

public record UserResponse(
        Integer id,
        String username,
        Role role,
        boolean enabled,
        Integer companyId,
        String companyName) {
    public static UserResponse from(ApplicationUser user) {
        Integer companyId = user.getCompany() == null ? null : user.getCompany().getCompanyId();
        String companyName = user.getCompany() == null ? null : user.getCompany().getCompanyName();
        return new UserResponse(
                user.getUserId(), user.getUsername(), user.getRole(), user.isEnabled(),
                companyId, companyName);
    }
}
