package com.stdace.neuroforge.models;

import com.stdace.neuroforge.enums.TaskStatus;
import com.stdace.neuroforge.enums.TaskPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "tasks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_task_code",
                        columnNames = {"project_id", "code"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseModel {

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(name = "story_points")
    private Integer storyPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_labels", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "label")
    @Builder.Default
    private List<String> labels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_dependencies", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "dependency_id")
    @Builder.Default
    private List<UUID> dependencies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_blockers", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "blocker_id")
    @Builder.Default
    private List<UUID> blockers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_comments", joinColumns = @JoinColumn(name = "task_id"))
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_attachments", joinColumns = @JoinColumn(name = "task_id"))
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "task_activities", joinColumns = @JoinColumn(name = "task_id"))
    @Builder.Default
    private List<TaskActivity> activityHistory = new ArrayList<>();
}
