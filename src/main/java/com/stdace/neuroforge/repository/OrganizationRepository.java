package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
import com.stdace.neuroforge.models.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsByNameIgnoreCase(String name);

    Optional<Organization> findBySlugIgnoreCase(String slug);

    Page<Organization> findByStatus(OrganizationStatus status, Pageable pageable);

    Page<Organization> findByType(OrganizationType type, Pageable pageable);

    Page<Organization> findByTypeAndStatus(OrganizationType type, OrganizationStatus status, Pageable pageable);
}
