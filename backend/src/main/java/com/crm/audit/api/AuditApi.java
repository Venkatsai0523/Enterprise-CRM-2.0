package com.crm.audit.api;

/**
 * Published cross-domain interface for recording compliance audit logs across the application.
 */
public interface AuditApi {

    void recordAudit(String entityName, String entityId, String action, String performedBy, String oldState, String newState);
}
