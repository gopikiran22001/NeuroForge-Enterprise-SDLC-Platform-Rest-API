package com.stdace.neuroforge.dto.organization;

import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
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
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private OrganizationType type;
    private OrganizationStatus status;
    private UUID ownerId;
    private String ownerEmail;
    private String ownerName;
    private Instant createdAt;
    private Instant updatedAt;
}
