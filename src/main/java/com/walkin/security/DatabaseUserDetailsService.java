package com.walkin.security;

import com.walkin.repository.ApplicationUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final ApplicationUserRepository users;
    public DatabaseUserDetailsService(ApplicationUserRepository users) { this.users = users; }

    @Override public UserDetails loadUserByUsername(String username) {
        var user = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return User.withUsername(user.getUsername()).password(user.getPasswordHash())
                .roles(user.getRole().name()).disabled(!user.isEnabled()).build();
    }
}
