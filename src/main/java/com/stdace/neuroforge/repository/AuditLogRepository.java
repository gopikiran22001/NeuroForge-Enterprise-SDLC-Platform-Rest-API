package com.stdace.neuroforge.repository;

import com.stdace.neuroforge.enums.AuditSeverity;
import com.stdace.neuroforge.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
    SELECT a FROM AuditLog a
    WHERE (:severity IS NULL OR a.severity = :severity)
      AND (:search IS NULL OR LOWER(a.actorEmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
           OR LOWER(a.action) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
           OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) 
           OR LOWER(a.actorName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<AuditLog> searchLogs(
            @Param("severity") AuditSeverity severity,
            @Param("search") String search,
            Pageable pageable
    );
}
