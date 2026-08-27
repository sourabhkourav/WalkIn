package com.walkin.security;

import com.walkin.entity.ApplicationUser;
import com.walkin.repository.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {
    private final ApplicationUserRepository users; private final PasswordEncoder encoder;
    private final String username; private final String password;
    public AdminAccountInitializer(ApplicationUserRepository users, PasswordEncoder encoder,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password) {
        this.users=users; this.encoder=encoder; this.username=username; this.password=password;
    }
    @Override public void run(ApplicationArguments args) {
        if (users.findByUsernameIgnoreCase(username).isEmpty()) {
            ApplicationUser admin=new ApplicationUser(); admin.setUsername(username.trim());
            admin.setPasswordHash(encoder.encode(password)); admin.setRole(ApplicationUser.Role.ADMIN); users.save(admin);
        }
    }
}
