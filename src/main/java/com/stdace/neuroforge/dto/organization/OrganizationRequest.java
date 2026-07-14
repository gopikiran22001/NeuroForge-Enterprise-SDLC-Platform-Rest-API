package com.stdace.neuroforge.dto.organization;

import com.stdace.neuroforge.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 200)
    private String name;

    /**
     * URL-friendly identifier, e.g. "acme-corp".
     * Must be lowercase alphanumeric with optional hyphens.
     */
    @NotBlank(message = "Slug is required")
    @Size(max = 100)
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug must be lowercase alphanumeric with hyphens only (e.g. 'acme-corp')"
    )
    private String slug;

    private String description;

    @NotNull(message = "Organization type is required")
    private OrganizationType type;

    /**
     * The user who will become the ORG_ADMIN/owner of this organization.
     */
    @NotNull(message = "Owner user ID is required")
    private UUID ownerUserId;

    private com.stdace.neuroforge.enums.OrganizationStatus status;
}
