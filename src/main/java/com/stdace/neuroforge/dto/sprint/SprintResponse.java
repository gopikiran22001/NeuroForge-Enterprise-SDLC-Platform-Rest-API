package com.stdace.neuroforge.dto.sprint;

import com.stdace.neuroforge.enums.SprintStatus;
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
public class SprintResponse {

    private UUID id;
    private String name;
    private String goal;
    private SprintStatus status;
    private Instant startDate;
    private Instant endDate;
    private UUID projectId;
    private String projectCode;
    private Instant createdAt;
    private Instant updatedAt;
}

