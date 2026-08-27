package com.walkin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "application_user")
public class ApplicationUser {
    public enum Role { ADMIN, RECRUITER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Role role;
    @Column(nullable = false)
    private boolean enabled = true;

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
