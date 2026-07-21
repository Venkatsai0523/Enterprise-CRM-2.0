package com.crm.audit.service;

import com.crm.audit.api.AuditApi;
import com.crm.audit.api.dto.AuditLogResponseDto;
import com.crm.audit.entity.AuditLog;
import com.crm.audit.mapper.AuditLogMapper;
import com.crm.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AuditService implements AuditApi {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void recordAudit(String entityName, String entityId, String action, String performedBy, String oldState, String newState) {
        AuditLog auditLog = AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy != null ? performedBy : "system")
                .oldState(oldState)
                .newState(newState)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Recorded audit log for {} ID '{}' action '{}'", entityName, entityId, action);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> getAuditLogs(String entityName, String entityId, String action, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findWithFilters(entityName, entityId, action, pageRequest)
                .map(auditLogMapper::toDto);
    }
}
