package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    // --- Org-scoped queries (for ORG_ADMIN) ---

    Page<User> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<User> findByOrganizationIdAndRole(UUID organizationId, UserRole role, Pageable pageable);

    Page<User> findByOrganizationIdAndStatus(UUID organizationId, UserStatus status, Pageable pageable);

    Page<User> findByOrganizationIdAndRoleAndStatus(UUID organizationId, UserRole role, UserStatus status, Pageable pageable);
}

