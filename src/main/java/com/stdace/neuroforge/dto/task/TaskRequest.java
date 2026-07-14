package com.stdace.neuroforge.dto.task;

import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Task status is required")
    private TaskStatus status;

    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    private Integer storyPoints;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID sprintId;

    private UUID assigneeId;

    private List<String> labels;

    private List<UUID> dependencies;

    private List<UUID> blockers;
}
