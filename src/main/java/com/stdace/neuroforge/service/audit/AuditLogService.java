package com.stdace.neuroforge.service.audit;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.audit.AuditLogResponse;
import com.stdace.neuroforge.enums.AuditSeverity;

import java.util.UUID;

public interface AuditLogService {

    void log(String action, String entityType, UUID entityId, AuditSeverity severity, String details);

    PageResponse<AuditLogResponse> search(AuditSeverity severity, String search, int page, int size);
}
