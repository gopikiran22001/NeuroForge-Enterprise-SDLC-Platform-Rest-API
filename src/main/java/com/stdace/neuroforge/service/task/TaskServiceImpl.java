package com.stdace.neuroforge.service.task;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.task.TaskRequest;
import com.stdace.neuroforge.dto.task.TaskResponse;
import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.TaskMapper;
import com.stdace.neuroforge.models.*;
import com.stdace.neuroforge.repository.ProjectRepository;
import com.stdace.neuroforge.repository.SprintRepository;
import com.stdace.neuroforge.repository.TaskRepository;
import com.stdace.neuroforge.repository.UserRepository;
import com.stdace.neuroforge.security.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.stdace.neuroforge.service.audit.AuditLogService;
import com.stdace.neuroforge.enums.AuditSeverity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final AuditLogService auditLogService;

    @Override
    public TaskResponse create(TaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        Sprint sprint = request.getSprintId() != null
                ? sprintRepository.findById(request.getSprintId()).orElse(null)
                : null;

        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId()).orElse(null)
                : null;

        Task task = taskMapper.toEntity(request, project, sprint, assignee);
        task.setCode(generateTaskCode(project));

        // Add creation activity log
        User creator = getCurrentUser();
        String creatorName = creator.getFirstName() + " " + creator.getLastName();
        task.getActivityHistory().add(TaskActivity.builder()
                .id(UUID.randomUUID())
                .actorName(creatorName)
                .action("Task created")
                .createdAt(Instant.now())
                .build());

        Task saved = taskRepository.save(task);

        auditLogService.log("Task Created", "Task", saved.getId(), AuditSeverity.INFO,
                "Created task: " + saved.getCode() + " - " + saved.getTitle() + " (Project ID: " + project.getId() + ")");

        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        Sprint sprint = request.getSprintId() != null
                ? sprintRepository.findById(request.getSprintId()).orElse(null)
                : null;

        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId()).orElse(null)
                : null;

        User actor = getCurrentUser();
        String actorName = actor.getFirstName() + " " + actor.getLastName();

        // Track changes for activity log
        if (task.getStatus() != request.getStatus()) {
            task.getActivityHistory().add(TaskActivity.builder()
                    .id(UUID.randomUUID())
                    .actorName(actorName)
                    .action("Status updated from " + task.getStatus() + " to " + request.getStatus())
                    .createdAt(Instant.now())
                    .build());
        }
        if (task.getAssignee() != assignee) {
            String newAssigneeName = assignee != null ? assignee.getFirstName() + " " + assignee.getLastName() : "Unassigned";
            task.getActivityHistory().add(TaskActivity.builder()
                    .id(UUID.randomUUID())
                    .actorName(actorName)
                    .action("Assignee updated to " + newAssigneeName)
                    .createdAt(Instant.now())
                    .build());
        }

        taskMapper.updateEntity(task, request, project, sprint, assignee);
        Task saved = taskRepository.save(task);

        auditLogService.log("Task Updated", "Task", saved.getId(), AuditSeverity.INFO,
                "Updated task: " + saved.getCode() + " - " + saved.getTitle());

        return taskMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        taskRepository.delete(task);

        auditLogService.log("Task Deleted", "Task", id, AuditSeverity.WARNING,
                "Deleted task: " + task.getCode() + " - " + task.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getById(UUID id) {
        return taskRepository.findById(id)
                .map(taskMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> search(UUID projectId, UUID sprintId, TaskStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponse> mapped = taskRepository.searchTasks(projectId, sprintId, status, search, pageable)
                .map(taskMapper::toResponse);
        return PageResponse.from(mapped);
    }

    @Override
    public TaskResponse addComment(UUID id, String text) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User actor = getCurrentUser();
        String actorName = actor.getFirstName() + " " + actor.getLastName();

        TaskComment comment = TaskComment.builder()
                .id(UUID.randomUUID())
                .authorName(actorName)
                .authorEmail(actor.getEmail())
                .text(text)
                .createdAt(Instant.now())
                .build();

        task.getComments().add(comment);
        
        task.getActivityHistory().add(TaskActivity.builder()
                .id(UUID.randomUUID())
                .actorName(actorName)
                .action("Added a comment")
                .createdAt(Instant.now())
                .build());

        Task saved = taskRepository.save(task);

        auditLogService.log("Task Comment Added", "Task", saved.getId(), AuditSeverity.INFO,
                "Added a comment on task: " + saved.getCode());

        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse addAttachment(UUID id, String name, String size, String url) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User actor = getCurrentUser();
        String actorName = actor.getFirstName() + " " + actor.getLastName();

        TaskAttachment attachment = TaskAttachment.builder()
                .id(UUID.randomUUID())
                .name(name)
                .size(size)
                .url(url)
                .createdAt(Instant.now())
                .build();

        task.getAttachments().add(attachment);

        task.getActivityHistory().add(TaskActivity.builder()
                .id(UUID.randomUUID())
                .actorName(actorName)
                .action("Uploaded attachment: " + name)
                .createdAt(Instant.now())
                .build());

        Task saved = taskRepository.save(task);

        auditLogService.log("Task Attachment Added", "Task", saved.getId(), AuditSeverity.INFO,
                "Uploaded attachment '" + name + "' to task: " + saved.getCode());

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskActivity> getActivityHistory(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        return new ArrayList<>(task.getActivityHistory());
    }

    private User getCurrentUser() {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found: " + userId));
    }

    private String generateTaskCode(Project project) {
        String maxCode = taskRepository.findMaxCodeByProjectId(project.getId());
        if (maxCode == null) {
            return project.getCode() + "-1";
        }
        try {
            String[] parts = maxCode.split("-");
            int nextNum = Integer.parseInt(parts[parts.length - 1]) + 1;
            return project.getCode() + "-" + nextNum;
        } catch (Exception e) {
            return project.getCode() + "-" + (int)(Math.random() * 1000);
        }
    }
}
