package com.stdace.neuroforge.service.project;

import com.stdace.neuroforge.enums.ProjectStatus;
import com.stdace.neuroforge.enums.TeamStatus;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.exception.ForbiddenException;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.project.ProjectRequest;
import com.stdace.neuroforge.dto.project.ProjectResponse;
import com.stdace.neuroforge.exception.BusinessException;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.ProjectMapper;
import com.stdace.neuroforge.repository.ProjectRepository;
import com.stdace.neuroforge.repository.TeamRepository;
import com.stdace.neuroforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponse create(ProjectRequest request) {
        validateCodeUnique(request.getCode(), null);
        User manager = getActiveUser(request.getProjectManagerId());
        Set<Team> teams = getActiveTeams(request.getTeamIds());
        validateDateRange(request.getStartDate(), request.getEndDate());
        Project project = projectMapper.toEntity(request, manager, teams);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        return projectRepository.findById(id).map(projectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID userId,UUID id) {
        if(!checkUser(userId, id)) {
            throw new ForbiddenException("User does not have access to this project");
        }
        return projectRepository.findById(id).map(projectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private boolean checkUser(UUID userId, UUID id) {
        return projectRepository.existsByProjectAndUser(id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> search(String search, ProjectStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> mapped = null;
        if(status != null) {
            mapped = projectRepository.findByStatus(status, pageable).map(projectMapper::toResponse);
        } else {
            mapped = projectRepository.findAll(pageable).map(projectMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> search(UUID userId, String search, ProjectStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> mapped = null;
        if(status != null) {
            mapped = projectRepository.findMyProjectsByStatus(userId, status, pageable).map(projectMapper::toResponse);
        } else {
            mapped = projectRepository.findMyProjects(userId, pageable).map(projectMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        validateCodeUnique(request.getCode(), id);
        User manager = getActiveUser(request.getProjectManagerId());
        Set<Team> teams = getActiveTeams(request.getTeamIds());
        validateDateRange(request.getStartDate(), request.getEndDate());
        projectMapper.updateEntity(project, request, manager, teams);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    public void delete(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        project.setStatus(ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    private void validateCodeUnique(String code, UUID currentId) {
        boolean duplicate = projectRepository.findAll().stream()
                .anyMatch(project -> project.getCode().equalsIgnoreCase(code) && !project.getId().equals(currentId));
        if (duplicate) {
            throw new DuplicateResourceException("Project code already exists");
        }
    }

    private User getActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Project manager must be ACTIVE");
        }
        return user;
    }

    private Set<Team> getActiveTeams(Set<UUID> teamIds) {
        return teamIds.stream().map(id -> {
            Team team = teamRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
            if (team.getStatus() != TeamStatus.ACTIVE) {
                throw new BusinessException("Only ACTIVE teams can be assigned to a project");
            }
            return team;
        }).collect(Collectors.toSet());
    }

    private void validateDateRange(Instant startDate, Instant endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Project end date must be after start date");
        }
    }

}
