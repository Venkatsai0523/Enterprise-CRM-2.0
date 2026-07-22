package com.crm.audit.controller;

import com.crm.audit.api.dto.AuditLogResponseDto;
import com.crm.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for querying system state transition audit logs (Admin/Manager compliance access)")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Query audit logs", description = "Retrieves paginated audit log entries filtered by entity name, entity ID, and action. Max page size: 100.")
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Max(value = 100, message = "Page size must not exceed 100") @RequestParam(defaultValue = "10") int size
    ) {
        Page<AuditLogResponseDto> response = auditService.getAuditLogs(entityName, entityId, action, page, size);
        return ResponseEntity.ok(response);
    }
}
