package com.stdace.neuroforge.mapper;

import com.stdace.neuroforge.models.AuditLog;
import com.stdace.neuroforge.dto.audit.AuditLogResponse;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        if (log == null) return null;
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorName(log.getActorName())
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .ipAddress(log.getIpAddress())
                .severity(log.getSeverity())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .build();
    }
}
