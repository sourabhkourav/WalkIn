package com.walkin.repository;

import com.walkin.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.walkin.entity.ApplicationUser.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    @EntityGraph(attributePaths = "company")
    Optional<ApplicationUser> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    long countByRoleAndEnabledTrue(Role role);
    @EntityGraph(attributePaths = "company")
    Page<ApplicationUser> findByCompany_CompanyId(Integer companyId, Pageable pageable);
}
