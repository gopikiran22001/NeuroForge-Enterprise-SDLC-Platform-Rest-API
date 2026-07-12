package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.sprint.SprintRequest;
import com.stdace.neuroforge.dto.sprint.SprintResponse;
import com.stdace.neuroforge.enums.SprintStatus;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.sprint.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<SprintResponse>> create(@Valid @RequestBody SprintRequest request) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.PROJECT_MANAGER)){
            sprintService.managerCheck(CurrentUserUtil.getCurrentUserId(), request);
        }
        return ResponseEntity.ok(ApiResponse.success("Sprint created successfully", sprintService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> getById(@PathVariable UUID id) {
        if(!CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            return ResponseEntity.ok(ApiResponse.success("Sprint retrieved successfully", sprintService.getById(CurrentUserUtil.getCurrentUserId(), id)));
        }
        return ResponseEntity.ok(ApiResponse.success("Sprint retrieved successfully", sprintService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SprintResponse>>> search(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SprintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if(!CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            UUID userId = CurrentUserUtil.getCurrentUserId();
            if(projectId != null){
                return ResponseEntity.ok(ApiResponse.success("Sprints retrieved successfully", sprintService.search(userId, search, projectId, status, page, size)));
            }
            return ResponseEntity.ok(ApiResponse.success("Sprints retrieved successfully", sprintService.search(userId, search, status, page, size)));
        }
        if(projectId != null){
            return ResponseEntity.ok(ApiResponse.success("Sprints retrieved successfully", sprintService.search(search, projectId, status, page, size)));
        }

        return ResponseEntity.ok(ApiResponse.success("Sprints retrieved successfully", sprintService.search(search, status, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<SprintResponse>> update(@PathVariable UUID id, @Valid @RequestBody SprintRequest request) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.PROJECT_MANAGER)){
            sprintService.managerCheck(CurrentUserUtil.getCurrentUserId(), id);
        }
        return ResponseEntity.ok(ApiResponse.success("Sprint updated successfully", sprintService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.PROJECT_MANAGER)){
            sprintService.managerCheck(CurrentUserUtil.getCurrentUserId(), id);
        }
        sprintService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sprint deleted successfully", null));
    }
}

