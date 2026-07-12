package com.stdace.neuroforge.dto.team;

import com.stdace.neuroforge.enums.TeamStatus;
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
public class TeamResponse {

    private UUID id;
    private String name;
    private String description;
    private TeamStatus status;
    private UUID teamLeaderId;
    private String teamLeaderEmail;
    private Set<UUID> memberIds;
    private Instant createdAt;
    private Instant updatedAt;
}

