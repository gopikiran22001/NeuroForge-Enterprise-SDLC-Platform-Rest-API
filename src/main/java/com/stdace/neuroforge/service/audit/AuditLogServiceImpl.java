package com.stdace.neuroforge.service.audit;

import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.audit.AuditLogResponse;
import com.stdace.neuroforge.enums.AuditSeverity;
import com.stdace.neuroforge.mapper.AuditLogMapper;
import com.stdace.neuroforge.models.AuditLog;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.repository.AuditLogRepository;
import com.stdace.neuroforge.repository.UserRepository;
import com.stdace.neuroforge.security.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public void log(String action, String entityType, UUID entityId, AuditSeverity severity, String details) {
        String actorName = "System";
        String actorEmail = "system@neuroforge.org";
        try {
            UUID currentUserId = CurrentUserUtil.getCurrentUserId();
            if (currentUserId != null) {
                User user = userRepository.findById(currentUserId).orElse(null);
                if (user != null) {
                    actorName = user.getFirstName() + " " + user.getLastName();
                    actorEmail = user.getEmail();
                }
            }
        } catch (Exception e) {
            // Gracefully fallback when unauthenticated
        }

        AuditLog log = AuditLog.builder()
                .actorName(actorName)
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress("127.0.0.1")
                .severity(severity)
                .details(details)
                .build();
        
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(AuditSeverity severity, String search, int page, int size) {
        // Sort audit logs by createdAt descending so recent logs appear first
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> mapped = auditLogRepository.searchLogs(severity, search, pageable)
                .map(auditLogMapper::toResponse);
        return PageResponse.from(mapped);
    }
}
