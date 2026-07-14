package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.organization.OrganizationRequest;
import com.stdace.neuroforge.dto.organization.OrganizationResponse;
import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
import com.stdace.neuroforge.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> create(
            @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully",
                organizationService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Organization retrieved successfully",
                organizationService.getById(id)));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Organization retrieved successfully",
                organizationService.getBySlug(slug)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Organization stats retrieved successfully",
                organizationService.getStats()));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrganizationResponse>>> search(
            @RequestParam(required = false) OrganizationType type,
            @RequestParam(required = false) OrganizationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Organizations retrieved successfully",
                organizationService.search(type, status, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully",
                organizationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", null));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrganizationResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Organization approved successfully",
                organizationService.approve(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<java.util.List<OrganizationResponse>>> getActiveOrganizations() {
        return ResponseEntity.ok(ApiResponse.success("Active organizations retrieved successfully",
                organizationService.getActiveOrganizations()));
    }
}
