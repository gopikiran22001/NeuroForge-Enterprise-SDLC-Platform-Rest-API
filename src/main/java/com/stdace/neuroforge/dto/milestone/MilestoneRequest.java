package com.stdace.neuroforge.dto.milestone;

import com.stdace.neuroforge.enums.MilestoneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MilestoneRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Instant dueDate;

    @NotNull
    private UUID projectId;

    @NotNull
    private MilestoneStatus status;
}
