package com.stdace.neuroforge.models;

import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(
        name = "organizations",
        indexes = {
                @Index(name = "idx_organization_slug", columnList = "slug", unique = true),
                @Index(name = "idx_organization_name", columnList = "name", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends BaseModel {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * URL-friendly unique identifier (e.g. "acme-corp").
     * Only lowercase letters, numbers, and hyphens are allowed.
     */
    @NotBlank
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
             message = "Slug must be lowercase alphanumeric with hyphens only (e.g. 'acme-corp')")
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status;

    /**
     * The primary ORG_ADMIN who owns this organization.
     * Nullable to allow creation before assigning an owner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Override
    protected void onCreate() {
        status = OrganizationStatus.PENDING_APPROVAL;
    }
}
