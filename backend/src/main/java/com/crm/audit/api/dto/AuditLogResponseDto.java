package com.crm.audit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private UUID id;
    private String entityName;
    private String entityId;
    private String action;
    private String performedBy;
    private String oldState;
    private String newState;
    private Instant timestamp;
}
