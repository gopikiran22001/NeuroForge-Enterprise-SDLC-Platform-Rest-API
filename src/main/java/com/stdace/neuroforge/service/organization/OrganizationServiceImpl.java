package com.stdace.neuroforge.service.organization;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.organization.OrganizationRequest;
import com.stdace.neuroforge.dto.organization.OrganizationResponse;
import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.exception.BusinessException;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ForbiddenException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.OrganizationMapper;
import com.stdace.neuroforge.models.Organization;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.repository.OrganizationRepository;
import com.stdace.neuroforge.repository.UserRepository;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.audit.AuditLogService;
import com.stdace.neuroforge.enums.AuditSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;
    private final AuditLogService auditLogService;

    @Override
    public OrganizationResponse create(OrganizationRequest request) {
        if (organizationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Organization name already exists: " + request.getName());
        }
        if (organizationRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new DuplicateResourceException("Organization slug already taken: " + request.getSlug());
        }

        User owner = userRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found: " + request.getOwnerUserId()));

        if (owner.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("SUPER_ADMIN cannot be assigned as an organization owner");
        }

        Organization org = organizationMapper.toEntity(request, owner);
        org.setStatus(OrganizationStatus.SUSPENDED);
        Organization saved = organizationRepository.save(org);

        // Promote owner to ORG_ADMIN and link to this organization
        owner.setRole(UserRole.ORG_ADMIN);
        owner.setOrganization(saved);
        userRepository.save(owner);

        auditLogService.log("Organization Registered", "Organization", saved.getId(), AuditSeverity.INFO,
                "Registered new organization: " + saved.getName() + " (Owner ID: " + owner.getId() + ")");

        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getById(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
        assertAccessAllowed(org);
        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getBySlug(String slug) {
        Organization org = organizationRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with slug: " + slug));
        assertAccessAllowed(org);
        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> search(OrganizationType type, OrganizationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (type != null && status != null) {
            return PageResponse.from(organizationRepository.findByTypeAndStatus(type, status, pageable)
                    .map(organizationMapper::toResponse));
        } else if (type != null) {
            return PageResponse.from(organizationRepository.findByType(type, pageable)
                    .map(organizationMapper::toResponse));
        } else if (status != null) {
            return PageResponse.from(organizationRepository.findByStatus(status, pageable)
                    .map(organizationMapper::toResponse));
        }
        return PageResponse.from(organizationRepository.findAll(pageable).map(organizationMapper::toResponse));
    }

    @Override
    public OrganizationResponse update(UUID id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
        assertAccessAllowed(org);

        if (!org.getName().equalsIgnoreCase(request.getName()) &&
                organizationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Organization name already exists: " + request.getName());
        }
        if (!org.getSlug().equalsIgnoreCase(request.getSlug()) &&
                organizationRepository.existsBySlugIgnoreCase(request.getSlug())) {
            throw new DuplicateResourceException("Organization slug already taken: " + request.getSlug());
        }

        User owner = userRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found: " + request.getOwnerUserId()));

        if (owner.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("SUPER_ADMIN cannot be assigned as an organization owner");
        }

        organizationMapper.updateEntity(org, request, owner);
        Organization saved = organizationRepository.save(org);

        auditLogService.log("Organization Updated", "Organization", saved.getId(), AuditSeverity.INFO,
                "Updated organization: " + saved.getName());

        return organizationMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
        org.setStatus(OrganizationStatus.DELETED);
        organizationRepository.save(org);

        auditLogService.log("Organization Deleted", "Organization", id, AuditSeverity.WARNING,
                "Deleted organization: " + org.getName());
    }

    @Override
    public OrganizationResponse approve(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));

        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();
        if (callerRole != UserRole.SUPER_ADMIN) {
            throw new com.stdace.neuroforge.exception.ForbiddenException("Only SUPER_ADMIN can approve organizations");
        }

        // Set organization status to ACTIVE
        org.setStatus(OrganizationStatus.ACTIVE);
        Organization saved = organizationRepository.save(org);

        // Set owner user status to ACTIVE
        if (saved.getOwner() != null) {
            User owner = saved.getOwner();
            owner.setStatus(com.stdace.neuroforge.enums.UserStatus.ACTIVE);
            userRepository.save(owner);
        }

        auditLogService.log("Organization Approved", "Organization", saved.getId(), AuditSeverity.INFO,
                "Approved organization: " + saved.getName());

        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<OrganizationResponse> getActiveOrganizations() {
        // Query active organizations page-by-page or just all active orgs. We can use findByStatus.
        // Let's use Pageable.unpaged() or standard PageRequest.of(0, 100)
        Pageable pageable = PageRequest.of(0, 500);
        return organizationRepository.findByStatus(OrganizationStatus.ACTIVE, pageable)
                .map(organizationMapper::toResponse)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", organizationRepository.count());
        stats.put("active", organizationRepository.countByStatus(OrganizationStatus.ACTIVE));
        stats.put("pendingApproval", organizationRepository.countByStatus(OrganizationStatus.PENDING_APPROVAL));
        stats.put("suspended", organizationRepository.countByStatus(OrganizationStatus.SUSPENDED));
        return stats;
    }

    /**
     * Ensures the current user may access the given organization.
     * SUPER_ADMIN can access any org; ORG_ADMIN can only access their own org.
     */
    private void assertAccessAllowed(Organization org) {
        UserRole role = CurrentUserUtil.getCurrentUserRole();
        if (role == UserRole.SUPER_ADMIN) {
            return; // full access
        }
        UUID currentOrgId = CurrentUserUtil.getCurrentUserOrganizationId();
        if (!org.getId().equals(currentOrgId)) {
            throw new ForbiddenException("Access denied to this organization");
        }
    }
}
