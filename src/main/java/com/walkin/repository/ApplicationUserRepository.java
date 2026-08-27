package com.walkin.repository;

import com.walkin.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.walkin.entity.ApplicationUser.Role;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    long countByRoleAndEnabledTrue(Role role);
}
