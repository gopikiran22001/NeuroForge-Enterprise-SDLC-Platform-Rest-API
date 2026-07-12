package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Sprint;
import com.stdace.neuroforge.dto.sprint.SprintRequest;
import com.stdace.neuroforge.dto.sprint.SprintResponse;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public Sprint toEntity(SprintRequest request, Project project) {
        Sprint sprint = new Sprint();
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(request.getStatus());
        sprint.setProject(project);
        return sprint;
    }

    public SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject() != null ? sprint.getProject().getId() : null)
                .projectCode(sprint.getProject() != null ? sprint.getProject().getCode() : null)
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }

    public void updateEntity(Sprint sprint, SprintRequest request, Project project) {
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(request.getStatus());
        sprint.setProject(project);
    }
}

