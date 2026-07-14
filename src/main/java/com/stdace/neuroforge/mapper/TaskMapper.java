package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.Project;
import com.stdace.neuroforge.models.Sprint;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.models.Task;
import com.stdace.neuroforge.dto.task.TaskRequest;
import com.stdace.neuroforge.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequest request, Project project, Sprint sprint, User assignee) {
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .storyPoints(request.getStoryPoints())
                .project(project)
                .sprint(sprint)
                .assignee(assignee)
                .labels(request.getLabels() != null ? new ArrayList<>(request.getLabels()) : new ArrayList<>())
                .dependencies(request.getDependencies() != null ? new ArrayList<>(request.getDependencies()) : new ArrayList<>())
                .blockers(request.getBlockers() != null ? new ArrayList<>(request.getBlockers()) : new ArrayList<>())
                .comments(new ArrayList<>())
                .attachments(new ArrayList<>())
                .activityHistory(new ArrayList<>())
                .build();
    }

    public TaskResponse toResponse(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .code(task.getCode())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectCode(task.getProject() != null ? task.getProject().getCode() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .sprintName(task.getSprint() != null ? task.getSprint().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? (task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName()) : null)
                .labels(task.getLabels() != null ? new ArrayList<>(task.getLabels()) : new ArrayList<>())
                .dependencies(task.getDependencies() != null ? new ArrayList<>(task.getDependencies()) : new ArrayList<>())
                .blockers(task.getBlockers() != null ? new ArrayList<>(task.getBlockers()) : new ArrayList<>())
                .comments(task.getComments() != null ? new ArrayList<>(task.getComments()) : new ArrayList<>())
                .attachments(task.getAttachments() != null ? new ArrayList<>(task.getAttachments()) : new ArrayList<>())
                .activityHistory(task.getActivityHistory() != null ? new ArrayList<>(task.getActivityHistory()) : new ArrayList<>())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public void updateEntity(Task task, TaskRequest request, Project project, Sprint sprint, User assignee) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setProject(project);
        task.setSprint(sprint);
        task.setAssignee(assignee);
        
        if (request.getLabels() != null) {
            task.getLabels().clear();
            task.getLabels().addAll(request.getLabels());
        }
        if (request.getDependencies() != null) {
            task.getDependencies().clear();
            task.getDependencies().addAll(request.getDependencies());
        }
        if (request.getBlockers() != null) {
            task.getBlockers().clear();
            task.getBlockers().addAll(request.getBlockers());
        }
    }
}
