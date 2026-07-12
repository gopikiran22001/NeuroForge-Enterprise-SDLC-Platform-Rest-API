package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Team;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.dto.project.ProjectRequest;
import com.stdace.neuroforge.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectRequest request, User projectManager, Set<Team> teams) {
        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode().toUpperCase());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setProjectManager(projectManager);
        project.setTeams(teams);
        return project;
    }

    public ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .projectManagerId(project.getProjectManager() != null ? project.getProjectManager().getId() : null)
                .projectManagerEmail(project.getProjectManager() != null ? project.getProjectManager().getEmail() : null)
                .teamIds(project.getTeams() == null ? Set.of() : project.getTeams().stream().map(Team::getId).collect(Collectors.toSet()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public void updateEntity(Project project, ProjectRequest request, User projectManager, Set<Team> teams) {
        project.setName(request.getName());
        project.setCode(request.getCode().toUpperCase());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setProjectManager(projectManager);
        project.setTeams(teams);
    }
}

