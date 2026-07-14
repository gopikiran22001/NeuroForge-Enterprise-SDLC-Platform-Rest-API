package com.stdace.neuroforge.service.organization;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.organization.OrganizationRequest;
import com.stdace.neuroforge.dto.organization.OrganizationResponse;
import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;

import java.util.UUID;

public interface OrganizationService {

    OrganizationResponse create(OrganizationRequest request);

    OrganizationResponse getById(UUID id);

    OrganizationResponse getBySlug(String slug);

    PageResponse<OrganizationResponse> search(OrganizationType type, OrganizationStatus status, int page, int size);

    OrganizationResponse update(UUID id, OrganizationRequest request);

    void delete(UUID id);

    OrganizationResponse approve(UUID id);

    java.util.List<OrganizationResponse> getActiveOrganizations();

    java.util.Map<String, Long> getStats();
}
