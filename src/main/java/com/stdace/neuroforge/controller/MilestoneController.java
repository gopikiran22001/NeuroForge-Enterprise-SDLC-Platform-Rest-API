package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.milestone.MilestoneRequest;
import com.stdace.neuroforge.dto.milestone.MilestoneResponse;
import com.stdace.neuroforge.enums.MilestoneStatus;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.exception.ForbiddenException;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.milestone.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody MilestoneRequest request) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.PROJECT_MANAGER)){
            milestoneService.managerCheck(CurrentUserUtil.getCurrentUserId(), request);
        }
        return ResponseEntity.ok(ApiResponse.success("Milestone created successfully", milestoneService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> getById(@PathVariable UUID id) {
        if(!CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            return ResponseEntity.ok(ApiResponse.success("Milestone retrieved successfully", milestoneService.getById(CurrentUserUtil.getCurrentUserId(),id)));
        }
        return ResponseEntity.ok(ApiResponse.success("Milestone retrieved successfully", milestoneService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MilestoneResponse>>> search(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MilestoneStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if(!CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            UUID userId = CurrentUserUtil.getCurrentUserId();
            if (projectId != null) {
                return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestoneService.search(userId, search, projectId, status, page, size)));
            }
            return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestoneService.search(userId, search, status, page, size)));
        }
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestoneService.search(search, projectId, status, page, size)));
        }
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestoneService.search(search, status, page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(@PathVariable UUID id, @Valid @RequestBody MilestoneRequest request) {
        if(!CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN) &&
            !milestoneService.isAllowed(id, CurrentUserUtil.getCurrentUserId())) {
            throw new ForbiddenException("You are not allowed to perform this action");
        }
        return ResponseEntity.ok(ApiResponse.success("Milestone updated successfully", milestoneService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.PROJECT_MANAGER)) {
            milestoneService.managerCheck(CurrentUserUtil.getCurrentUserId(), id);
        }
        milestoneService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted successfully", null));
    }
}

