package com.techgroup.techcop.service.audit;

import com.techgroup.techcop.model.dto.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    void log(String userIdentifier, String action, String entityName, String entityId, String details);

    List<AuditLogResponse> getRecentLogs();
}
