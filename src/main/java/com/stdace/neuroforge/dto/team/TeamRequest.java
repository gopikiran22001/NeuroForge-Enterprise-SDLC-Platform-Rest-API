package com.stdace.neuroforge.dto.team;

import com.stdace.neuroforge.enums.TeamStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private UUID teamLeaderId;

    @NotEmpty
    private Set<UUID> memberIds;

    @NotNull
    private TeamStatus status;
}

