package com.stdace.neuroforge.controller;

import com.stdace.neuroforge.common.ApiResponse;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.audit.AuditLogResponse;
import com.stdace.neuroforge.enums.AuditSeverity;
import com.stdace.neuroforge.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> search(
            @RequestParam(required = false) AuditSeverity severity,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Audit logs retrieved successfully",
                auditLogService.search(severity, search, page, size)
        ));
    }
}
