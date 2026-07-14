package com.stdace.neuroforge.service.sprint;

import com.stdace.neuroforge.enums.ProjectStatus;
import com.stdace.neuroforge.enums.SprintStatus;
import com.stdace.neuroforge.exception.ForbiddenException;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Sprint;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.sprint.SprintRequest;
import com.stdace.neuroforge.dto.sprint.SprintResponse;
import com.stdace.neuroforge.exception.BusinessException;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.SprintMapper;
import com.stdace.neuroforge.repository.ProjectRepository;
import com.stdace.neuroforge.repository.SprintRepository;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.audit.AuditLogService;
import com.stdace.neuroforge.enums.AuditSeverity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;
    private final AuditLogService auditLogService;


    @Override
    public void managerCheck(UUID currentUserId, SprintRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(()->new ResourceNotFoundException("Project not found: " + request.getProjectId()));
        if(!project.getProjectManager().getId().equals(currentUserId)){
            throw new ForbiddenException("User does not have access to this project");
        }
    }

    @Override
    public void managerCheck(UUID currentUserId, UUID id) {
         Sprint sprint = sprintRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Sprint not found: " + id));
         Project project = projectRepository.findById(sprint.getProject().getId()).orElseThrow(()->new ResourceNotFoundException("Project not found: " + sprint.getProject().getId()));
        if(!project.getProjectManager().getId().equals(currentUserId)){
            throw new ForbiddenException("User does not have access to this project");
        }
    }

    @Override
    public SprintResponse create(SprintRequest request) {
        Project project = getActiveProject(request.getProjectId());
        validateSprintDatesWithinProject(request.getStartDate(), request.getEndDate(), project);
        ensureSingleActiveSprint(project, null, request.getStatus());
        Sprint sprint = sprintMapper.toEntity(request, project);
        Sprint saved = sprintRepository.save(sprint);
        
        auditLogService.log("Sprint Created", "Sprint", saved.getId(), AuditSeverity.INFO, 
                "Created sprint: " + saved.getName() + " for project ID: " + project.getId());
        
        return sprintMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getById(UUID id) {
        return sprintRepository.findById(id).map(sprintMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getById(UUID userId, UUID id) {
        if(!checkUser(userId, id)) {
            throw new ForbiddenException("User does not have access to this sprint");
        }
        return sprintRepository.findById(id).map(sprintMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintResponse> search(String search, SprintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SprintResponse> mapped = null;
        if (status != null) {
            mapped = sprintRepository.findByStatus(status, pageable).map(sprintMapper::toResponse);
        } else {
            mapped = sprintRepository.findAll(pageable).map(sprintMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintResponse> search(String search, UUID projectId, SprintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SprintResponse> mapped = null;
        if (status != null) {
            mapped = sprintRepository.findByProjectIdAndStatus(projectId, status, pageable).map(sprintMapper::toResponse);
        } else {
            mapped = sprintRepository.findByProjectId(projectId, pageable).map(sprintMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintResponse> search(UUID userId, String search, SprintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SprintResponse> mapped = null;
        if (status != null) {
            mapped = sprintRepository.findByUserIdAndStatus(userId,status, pageable).map(sprintMapper::toResponse);
        } else {
            mapped = sprintRepository.findByUserId(userId, pageable).map(sprintMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SprintResponse> search(UUID userId, String search, UUID projectId, SprintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SprintResponse> mapped = null;
        if (status != null) {
            mapped = sprintRepository
                    .findByProjectIdAndStatus(userId, projectId, status, pageable)
                    .map(sprintMapper::toResponse);
        } else {
            mapped = sprintRepository
                    .findByProjectId(userId, projectId, pageable)
                    .map(sprintMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    public SprintResponse update(UUID id, SprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + id));
        
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessException("Completed sprints cannot be modified");
        }
        
        Project project = getActiveProject(request.getProjectId());
        validateSprintDatesWithinProject(request.getStartDate(), request.getEndDate(), project);
        ensureSingleActiveSprint(project, id, request.getStatus());
        sprintMapper.updateEntity(sprint, request, project);
        Sprint saved = sprintRepository.save(sprint);
        
        auditLogService.log("Sprint Updated", "Sprint", saved.getId(), AuditSeverity.INFO, 
                "Updated sprint: " + saved.getName() + " (Status: " + saved.getStatus() + ")");
        
        return sprintMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found: " + id));
        
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessException("Completed sprints cannot be deleted");
        }
        
        sprint.setStatus(SprintStatus.CANCELLED);
        sprintRepository.save(sprint);
        
        auditLogService.log("Sprint Cancelled/Deleted", "Sprint", sprint.getId(), AuditSeverity.WARNING, 
                "Sprint status set to CANCELLED for: " + sprint.getName());
    }

    private Project getActiveProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        if (project.getStatus() != ProjectStatus.ACTIVE && project.getStatus() != ProjectStatus.PLANNING) {
            throw new BusinessException("Sprint must belong to an active or planning project");
        }
        return project;
    }

    private void validateSprintDatesWithinProject(Instant sprintStart, Instant sprintEnd, Project project) {
        if (project.getStartDate() != null && sprintStart.isBefore(project.getStartDate())) {
            throw new BusinessException("Sprint start date must be within project dates");
        }
        if (project.getEndDate() != null && sprintEnd.isAfter(project.getEndDate())) {
            throw new BusinessException("Sprint end date must be within project dates");
        }
        if (sprintEnd.isBefore(sprintStart)) {
            throw new BusinessException("Sprint end date must be after start date");
        }
    }

    private boolean checkUser(UUID userId, UUID id) {
        return sprintRepository.existsBySprintAndUser(userId, id);
    }

    private void ensureSingleActiveSprint(Project project, UUID currentSprintId, SprintStatus status) {
        if (status == SprintStatus.ACTIVE) {
            sprintRepository.findByProjectAndStatus(project, SprintStatus.ACTIVE)
                    .filter(sprint -> !sprint.getId().equals(currentSprintId))
                    .ifPresent(sprint -> { throw new BusinessException("Only one ACTIVE sprint is allowed per project"); });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getStats(UUID projectId) {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        com.stdace.neuroforge.enums.UserRole callerRole = CurrentUserUtil.getCurrentUserRole();
        if (callerRole == com.stdace.neuroforge.enums.UserRole.SUPER_ADMIN || callerRole == com.stdace.neuroforge.enums.UserRole.ORG_ADMIN) {
            if (projectId != null) {
                stats.put("total", sprintRepository.countByProjectId(projectId));
                stats.put("active", sprintRepository.countByProjectIdAndStatus(projectId, SprintStatus.ACTIVE));
                stats.put("planned", sprintRepository.countByProjectIdAndStatus(projectId, SprintStatus.PLANNED));
                stats.put("completed", sprintRepository.countByProjectIdAndStatus(projectId, SprintStatus.COMPLETED));
            } else {
                stats.put("total", sprintRepository.count());
                stats.put("active", sprintRepository.countByStatus(SprintStatus.ACTIVE));
                stats.put("planned", sprintRepository.countByStatus(SprintStatus.PLANNED));
                stats.put("completed", sprintRepository.countByStatus(SprintStatus.COMPLETED));
            }
        } else {
            UUID userId = CurrentUserUtil.getCurrentUserId();
            if (projectId != null) {
                stats.put("total", sprintRepository.countByProjectId(userId, projectId));
                stats.put("active", sprintRepository.countByProjectIdAndStatus(userId, projectId, SprintStatus.ACTIVE));
                stats.put("planned", sprintRepository.countByProjectIdAndStatus(userId, projectId, SprintStatus.PLANNED));
                stats.put("completed", sprintRepository.countByProjectIdAndStatus(userId, projectId, SprintStatus.COMPLETED));
            } else {
                stats.put("total", sprintRepository.countByUserId(userId));
                stats.put("active", sprintRepository.countByUserIdAndStatus(userId, SprintStatus.ACTIVE));
                stats.put("planned", sprintRepository.countByUserIdAndStatus(userId, SprintStatus.PLANNED));
                stats.put("completed", sprintRepository.countByUserIdAndStatus(userId, SprintStatus.COMPLETED));
            }
        }
        return stats;
    }
}
