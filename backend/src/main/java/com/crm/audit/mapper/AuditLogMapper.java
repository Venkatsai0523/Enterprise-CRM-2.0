package com.crm.audit.mapper;

import com.crm.audit.api.dto.AuditLogResponseDto;
import com.crm.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponseDto toDto(AuditLog auditLog);
}
