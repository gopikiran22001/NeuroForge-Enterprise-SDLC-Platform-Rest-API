package com.stdace.neuroforge.models;

import com.stdace.neuroforge.enums.AuditSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseModel {

    @NotBlank
    @Column(nullable = false)
    private String actorName;

    @NotBlank
    @Column(nullable = false)
    private String actorEmail;

    @NotBlank
    @Column(nullable = false)
    private String action;

    private String entityType;

    private UUID entityId;

    private String ipAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String details;
}
