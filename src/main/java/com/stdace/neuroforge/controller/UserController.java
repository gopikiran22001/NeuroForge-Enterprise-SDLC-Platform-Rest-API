package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.user.UserRequest;
import com.stdace.neuroforge.dto.user.UserResponse;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile() {
        UUID uuid = CurrentUserUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userService.getById(uuid)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User created successfully", userService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<?>> search(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if(id != null) {
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userService.getById(id)));
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userService.search(search, status, role, page, size)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> update(@RequestParam(required = false) UUID id, @Valid @RequestBody UserRequest request) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            if(id == null)
                throw new ResourceNotFoundException("User not found: " + id);
        } else {
            id = (UUID) CurrentUserUtil.getCurrentUserId();
        }
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.update(id, request)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam(required = false) UUID id) {
        if(CurrentUserUtil.getCurrentUserRole().equals(UserRole.ADMIN)) {
            if(id == null)
                throw new ResourceNotFoundException("User not found: " + id);
        } else {
                id = (UUID) CurrentUserUtil.getCurrentUserId();
        }
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}

