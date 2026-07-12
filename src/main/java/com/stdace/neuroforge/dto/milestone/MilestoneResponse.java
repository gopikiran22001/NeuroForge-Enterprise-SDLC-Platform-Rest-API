package com.stdace.neuroforge.dto.milestone;

import com.stdace.neuroforge.enums.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {

    private UUID id;
    private String name;
    private String description;
    private MilestoneStatus status;
    private Instant dueDate;
    private UUID projectId;
    private String projectCode;
    private Instant createdAt;
    private Instant updatedAt;
}

