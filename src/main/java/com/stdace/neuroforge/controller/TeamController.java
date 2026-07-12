package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.team.TeamRequest;
import com.stdace.neuroforge.dto.team.TeamResponse;
import com.stdace.neuroforge.enums.TeamStatus;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.team.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team created successfully", teamService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully", teamService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TeamResponse>>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TeamStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", teamService.search(search, status, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TeamResponse>> update(@PathVariable UUID id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", teamService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}

