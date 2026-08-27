package com.walkin.dto;

import com.walkin.entity.ApplicationUser;
import com.walkin.entity.ApplicationUser.Role;

public record UserResponse(Integer id, String username, Role role, boolean enabled) {
    public static UserResponse from(ApplicationUser user) {
        return new UserResponse(user.getUserId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}
