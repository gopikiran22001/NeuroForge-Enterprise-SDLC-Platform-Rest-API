package com.stdace.neuroforge.dto.project;

import com.stdace.neuroforge.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private ProjectStatus status;
    private Instant startDate;
    private Instant endDate;
    private UUID projectManagerId;
    private String projectManagerEmail;
    private Set<UUID> teamIds;
    private Instant createdAt;
    private Instant updatedAt;
}

