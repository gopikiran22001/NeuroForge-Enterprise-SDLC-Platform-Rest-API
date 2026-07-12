package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.Milestone;
import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.dto.milestone.MilestoneRequest;
import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public Milestone toEntity(MilestoneRequest request, Project project) {
        Milestone milestone = new Milestone();
        milestone.setName(request.getName());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        milestone.setStatus(request.getStatus());
        milestone.setProject(project);
        return milestone;
    }

    public MilestoneResponse toResponse(Milestone milestone) {
        return MilestoneResponse.builder()
                .id(milestone.getId())
                .name(milestone.getName())
                .description(milestone.getDescription())
                .status(milestone.getStatus())
                .dueDate(milestone.getDueDate())
                .projectId(milestone.getProject() != null ? milestone.getProject().getId() : null)
                .projectCode(milestone.getProject() != null ? milestone.getProject().getCode() : null)
                .createdAt(milestone.getCreatedAt())
                .updatedAt(milestone.getUpdatedAt())
                .build();
    }

    public void updateEntity(Milestone milestone, MilestoneRequest request, Project project) {
        milestone.setName(request.getName());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        milestone.setStatus(request.getStatus());
        milestone.setProject(project);
    }
}

