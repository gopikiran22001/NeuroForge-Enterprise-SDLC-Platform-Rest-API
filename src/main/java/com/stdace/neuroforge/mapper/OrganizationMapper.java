package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.dto.organization.OrganizationRequest;
import com.stdace.neuroforge.dto.organization.OrganizationResponse;
import com.stdace.neuroforge.models.Organization;
import com.stdace.neuroforge.models.User;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toEntity(OrganizationRequest request, User owner) {
        Organization org = new Organization();
        org.setName(request.getName());
        org.setSlug(request.getSlug().toLowerCase());
        org.setDescription(request.getDescription());
        org.setType(request.getType());
        org.setOwner(owner);
        if (request.getStatus() != null) {
            org.setStatus(request.getStatus());
        }
        return org;
    }

    public OrganizationResponse toResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .description(org.getDescription())
                .type(org.getType())
                .status(org.getStatus())
                .ownerId(org.getOwner() != null ? org.getOwner().getId() : null)
                .ownerEmail(org.getOwner() != null ? org.getOwner().getEmail() : null)
                .ownerName(org.getOwner() != null ? org.getOwner().getFirstName() + " " + org.getOwner().getLastName() : null)
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    public void updateEntity(Organization org, OrganizationRequest request, User owner) {
        org.setName(request.getName());
        org.setSlug(request.getSlug().toLowerCase());
        org.setDescription(request.getDescription());
        org.setType(request.getType());
        org.setOwner(owner);
        if (request.getStatus() != null) {
            org.setStatus(request.getStatus());
        }
    }
}
