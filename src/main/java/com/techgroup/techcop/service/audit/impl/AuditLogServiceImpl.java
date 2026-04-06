package com.techgroup.techcop.service.audit.impl;

import com.techgroup.techcop.model.dto.AuditLogResponse;
import com.techgroup.techcop.model.entity.AuditLog;
import com.techgroup.techcop.repository.AuditLogRepository;
import com.techgroup.techcop.service.audit.AuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String userIdentifier, String action, String entityName, String entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserIdentifier(trimToValue(userIdentifier, "system"));
        auditLog.setAction(trimToValue(action, "UNKNOWN_ACTION"));
        auditLog.setEntityName(trimToValue(entityName, "UNKNOWN_ENTITY"));
        auditLog.setEntityId(trimToNull(entityId));
        auditLog.setDetails(trimToNull(details));
        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogResponse> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    private String trimToValue(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
