package com.stdace.neuroforge.service.milestone;

import com.stdace.neuroforge.enums.MilestoneStatus;
import com.stdace.neuroforge.exception.ForbiddenException;
import com.stdace.neuroforge.models.Milestone;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.milestone.MilestoneRequest;
import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.MilestoneMapper;
import com.stdace.neuroforge.repository.MilestoneRepository;
import com.stdace.neuroforge.repository.ProjectRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;

    @Override
    public void managerCheck(UUID currentUserId, MilestoneRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));
        if(!project.getProjectManager().getId().equals(currentUserId)) {
            throw new ForbiddenException("User does not have access to this project");
        }
    }

    @Override
    public void managerCheck(UUID currentUserId, UUID milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + milestoneId));
        Project project = projectRepository.findById(milestone.getProject().getId()).orElseThrow(() -> new ResourceNotFoundException("Project not found: " + milestone.getProject().getId()));
        if(!project.getProjectManager().getId().equals(currentUserId)) {
            throw new ForbiddenException("User does not have access to this project");
        }
    }

    @Override
    public MilestoneResponse create(MilestoneRequest request) {
        Project project = getProject(request.getProjectId());
        Milestone milestone = milestoneMapper.toEntity(request, project);
        return milestoneMapper.toResponse(milestoneRepository.save(milestone));
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse getById(UUID id) {
        return milestoneRepository.findById(id).map(milestoneMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneResponse getById(UUID userId, UUID id) {
        return milestoneRepository.findByIdAndUserId(id, userId)
                .map(milestoneMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> search(String search, MilestoneStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MilestoneResponse> mapped = null;
        if (status != null) {
            mapped = milestoneRepository.findByStatus(status, pageable).map(milestoneMapper::toResponse);
        } else {
            mapped = milestoneRepository.findAll(pageable).map(milestoneMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> search(String search, UUID projectId, MilestoneStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MilestoneResponse> mapped = null;
        if (status != null) {
            mapped = milestoneRepository.findByProjectIdAndStatus(projectId,status,pageable).map(milestoneMapper::toResponse);
        } else {
            mapped = milestoneRepository.findByProjectId(projectId, pageable).map(milestoneMapper::toResponse);
        }
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> search(
            UUID userId,
            String search,
            UUID projectId,
            MilestoneStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MilestoneResponse> mapped;

        if (status != null) {
            mapped = milestoneRepository
                    .findByProjectIdAndUserIdAndStatus(projectId, userId, status, pageable)
                    .map(milestoneMapper::toResponse);
        } else {
            mapped = milestoneRepository
                    .findByProjectIdAndUserId(projectId, userId, pageable)
                    .map(milestoneMapper::toResponse);
        }

        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MilestoneResponse> search(
            UUID userId,
            String search,
            MilestoneStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MilestoneResponse> mapped;

        if (status != null) {
            mapped = milestoneRepository
                    .findByUserIdAndStatus(userId, status, pageable)
                    .map(milestoneMapper::toResponse);
        } else {
            mapped = milestoneRepository
                    .findByUserId(userId, pageable)
                    .map(milestoneMapper::toResponse);
        }

        return PageResponse.from(mapped);
    }



    @Override
    public MilestoneResponse update(UUID id, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + id));
        Project project = getProject(request.getProjectId());
        milestoneMapper.updateEntity(milestone, request, project);
        return milestoneMapper.toResponse(milestoneRepository.save(milestone));
    }

    @Override
    public void delete(UUID id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found: " + id));
        milestone.setStatus(MilestoneStatus.CANCELLED);
        milestoneRepository.save(milestone);
    }

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    public boolean isAllowed(UUID milestoneId,UUID userId) {
        return milestoneRepository.isProjectManagerOrTeamLeadForMilestoneProject(milestoneId, userId);
    }

}
