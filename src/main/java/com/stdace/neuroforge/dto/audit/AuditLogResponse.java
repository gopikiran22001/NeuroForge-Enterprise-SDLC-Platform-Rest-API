package com.stdace.neuroforge.dto.audit;

import com.stdace.neuroforge.enums.AuditSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private String actorName;
    private String actorEmail;
    private String action;
    private String entityType;
    private UUID entityId;
    private String ipAddress;
    private AuditSeverity severity;
    private String details;
    private Instant timestamp;
}
