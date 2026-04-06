package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String user;
    private String action;
    private String entity;
    private String entityId;
    private String details;
    private LocalDateTime timestamp;

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setUser(auditLog.getUserIdentifier());
        response.setAction(auditLog.getAction());
        response.setEntity(auditLog.getEntityName());
        response.setEntityId(auditLog.getEntityId());
        response.setDetails(auditLog.getDetails());
        response.setTimestamp(auditLog.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
