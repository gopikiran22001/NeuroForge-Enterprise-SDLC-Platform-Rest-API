package com.stdace.neuroforge.dto.task;

import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.enums.TaskPriority;
import com.stdace.neuroforge.models.TaskComment;
import com.stdace.neuroforge.models.TaskAttachment;
import com.stdace.neuroforge.models.TaskActivity;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;
    private String title;
    private String code;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Integer storyPoints;
    private UUID projectId;
    private String projectCode;
    private String projectName;
    private UUID sprintId;
    private String sprintName;
    private UUID assigneeId;
    private String assigneeName;
    private List<String> labels;
    private List<UUID> dependencies;
    private List<UUID> blockers;
    private List<TaskComment> comments;
    private List<TaskAttachment> attachments;
    private List<TaskActivity> activityHistory;
    private Instant createdAt;
    private Instant updatedAt;
}
