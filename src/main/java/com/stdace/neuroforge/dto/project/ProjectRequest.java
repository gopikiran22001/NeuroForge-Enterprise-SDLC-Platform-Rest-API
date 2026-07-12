package com.stdace.neuroforge.dto.project;

import com.stdace.neuroforge.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ProjectRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    @NotNull
    private Instant startDate;

    private Instant endDate;

    @NotNull
    private UUID projectManagerId;

    @NotEmpty
    private Set<UUID> teamIds;

    @NotNull
    private ProjectStatus status;
}
