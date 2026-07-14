package com.stdace.neuroforge.service.user;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.models.Organization;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.user.UserRequest;
import com.stdace.neuroforge.dto.user.UserResponse;
import com.stdace.neuroforge.exception.BusinessException;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.UserMapper;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        // ORG_ADMIN cannot create SUPER_ADMIN users
        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();
        if (callerRole == UserRole.ORG_ADMIN && request.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("ORG_ADMIN cannot create a SUPER_ADMIN user");
        }
        User user = userMapper.toEntity(request, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        // If caller is ORG_ADMIN, auto-assign their organization to the new user
        if (callerRole == UserRole.ORG_ADMIN) {
            UUID orgId = CurrentUserUtil.getCurrentUserOrganizationId();
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));
            user.setOrganization(org);
        }
        User saved = userRepository.save(user);
 
        auditLogService.log("User Created", "User", saved.getId(), AuditSeverity.INFO,
                "Created user account for: " + saved.getEmail() + " (Role: " + saved.getRole() + ")");
 
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userRepository.findById(id).map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String search, UserStatus status, UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();

        // ORG_ADMIN: always scope to their organization
        if (callerRole == UserRole.ORG_ADMIN) {
            UUID orgId = CurrentUserUtil.getCurrentUserOrganizationId();
            if (role != null && status != null) {
                return PageResponse.from(userRepository.findByOrganizationIdAndRoleAndStatus(orgId, role, status, pageable).map(userMapper::toResponse));
            } else if (role != null) {
                return PageResponse.from(userRepository.findByOrganizationIdAndRole(orgId, role, pageable).map(userMapper::toResponse));
            } else if (status != null) {
                return PageResponse.from(userRepository.findByOrganizationIdAndStatus(orgId, status, pageable).map(userMapper::toResponse));
            }
            return PageResponse.from(userRepository.findByOrganizationId(orgId, pageable).map(userMapper::toResponse));
        }

        // SUPER_ADMIN: platform-wide search
        if (role != null && status != null) {
            return PageResponse.from(userRepository.findByRoleAndStatus(role, status, pageable).map(userMapper::toResponse));
        } else if (role != null) {
            return PageResponse.from(userRepository.findByRole(role, pageable).map(userMapper::toResponse));
        } else if (status != null) {
            return PageResponse.from(userRepository.findByStatus(status, pageable).map(userMapper::toResponse));
        }
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        userMapper.updateEntity(user, request, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        User saved = userRepository.save(user);
 
        auditLogService.log("User Updated", "User", saved.getId(), AuditSeverity.INFO,
                "Updated user account profile for: " + saved.getEmail());
 
        return userMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
 
        auditLogService.log("User Account Deleted", "User", id, AuditSeverity.WARNING,
                "Deactivated/Deleted user account: " + user.getEmail());
    }

    @Override
    public UserResponse approve(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();
        if (callerRole == UserRole.SUPER_ADMIN) {
            // SUPER_ADMIN can only approve ORG_ADMINs
            if (user.getRole() != UserRole.ORG_ADMIN) {
                throw new com.stdace.neuroforge.exception.ForbiddenException("SUPER_ADMIN can only approve ORG_ADMIN users");
            }
        } else if (callerRole == UserRole.ORG_ADMIN) {
            UUID orgId = CurrentUserUtil.getCurrentUserOrganizationId();
            if (user.getOrganization() == null || !user.getOrganization().getId().equals(orgId)) {
                throw new com.stdace.neuroforge.exception.ForbiddenException("You can only approve members of your own organization");
            }
        } else {
            throw new com.stdace.neuroforge.exception.ForbiddenException("Only ORG_ADMIN or SUPER_ADMIN can approve members");
        }

        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
 
        auditLogService.log("User Approved", "User", saved.getId(), AuditSeverity.INFO,
                "Approved pending user account: " + saved.getEmail());
 
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getPending(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();

        if (callerRole == UserRole.SUPER_ADMIN) {
            // SUPER_ADMIN can view all pending platform users (mostly pending ORG_ADMINs)
            return PageResponse.from(userRepository.findByStatus(UserStatus.PENDING_APPROVAL, pageable)
                    .map(userMapper::toResponse));
        }

        if (callerRole == UserRole.ORG_ADMIN) {
            UUID orgId = CurrentUserUtil.getCurrentUserOrganizationId();
            return PageResponse.from(userRepository.findByOrganizationIdAndStatus(orgId, UserStatus.PENDING_APPROVAL, pageable)
                    .map(userMapper::toResponse));
        }

        throw new com.stdace.neuroforge.exception.ForbiddenException("Access denied");
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        UserRole callerRole = CurrentUserUtil.getCurrentUserRole();
        if (callerRole == UserRole.ORG_ADMIN) {
            UUID orgId = CurrentUserUtil.getCurrentUserOrganizationId();
            stats.put("total", userRepository.countByOrganizationId(orgId));
            stats.put("active", userRepository.countByOrganizationIdAndStatus(orgId, UserStatus.ACTIVE));
            stats.put("pending", userRepository.countByOrganizationIdAndStatus(orgId, UserStatus.PENDING_APPROVAL));
            stats.put("suspended", userRepository.countByOrganizationIdAndStatusIn(orgId, java.util.List.of(UserStatus.INACTIVE, UserStatus.DELETED)));
        } else {
            stats.put("total", userRepository.count());
            stats.put("active", userRepository.countByStatus(UserStatus.ACTIVE));
            stats.put("pending", userRepository.countByStatus(UserStatus.PENDING_APPROVAL));
            stats.put("suspended", userRepository.countByStatusIn(java.util.List.of(UserStatus.INACTIVE, UserStatus.DELETED)));
        }
        return stats;
    }
}
