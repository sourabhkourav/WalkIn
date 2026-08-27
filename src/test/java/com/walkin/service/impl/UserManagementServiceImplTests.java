package com.walkin.service.impl;

import com.walkin.dto.*;
import com.walkin.entity.ApplicationUser;
import com.walkin.entity.ApplicationUser.Role;
import com.walkin.exception.ResourceConflictException;
import com.walkin.repository.ApplicationUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTests {
    @Mock ApplicationUserRepository users;
    @Mock PasswordEncoder encoder;
    @InjectMocks UserManagementServiceImpl service;

    @Test void createHashesPasswordAndNeverReturnsIt() {
        when(encoder.encode("strong-password")).thenReturn("{bcrypt}hash");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UserResponse response = service.create(new CreateUserRequest(" Recruiter.One ", "strong-password", Role.RECRUITER));
        ArgumentCaptor<ApplicationUser> saved = ArgumentCaptor.forClass(ApplicationUser.class);
        verify(users).save(saved.capture());
        assertEquals("Recruiter.One", response.username());
        assertEquals("{bcrypt}hash", saved.getValue().getPasswordHash());
    }

    @Test void duplicateUsernameIsRejectedBeforeEncoding() {
        when(users.existsByUsernameIgnoreCase("existing")).thenReturn(true);
        assertThrows(ResourceConflictException.class,
                () -> service.create(new CreateUserRequest("existing", "strong-password", Role.RECRUITER)));
        verifyNoInteractions(encoder);
    }

    @Test void lastEnabledAdminCannotBeDisabled() {
        ApplicationUser admin = user(Role.ADMIN, true);
        when(users.findById(1)).thenReturn(Optional.of(admin));
        when(users.countByRoleAndEnabledTrue(Role.ADMIN)).thenReturn(1L);
        assertThrows(ResourceConflictException.class,
                () -> service.update(1, new UpdateUserRequest(Role.ADMIN, false)));
        verify(users, never()).save(any());
    }

    @Test void passwordResetHashesNewPassword() {
        ApplicationUser recruiter = user(Role.RECRUITER, true);
        when(users.findById(2)).thenReturn(Optional.of(recruiter));
        when(encoder.encode("new-password-123")).thenReturn("{bcrypt}new-hash");
        service.resetPassword(2, new ResetPasswordRequest("new-password-123"));
        assertEquals("{bcrypt}new-hash", recruiter.getPasswordHash());
        verify(users).save(recruiter);
    }

    private ApplicationUser user(Role role, boolean enabled) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user"); user.setPasswordHash("hash"); user.setRole(role); user.setEnabled(enabled);
        return user;
    }
}
