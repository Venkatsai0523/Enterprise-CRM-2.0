package com.crm.audit.mapper;

import com.crm.audit.api.dto.AuditLogResponseDto;
import com.crm.audit.entity.AuditLog;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T13:58:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Override
    public AuditLogResponseDto toDto(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }

        AuditLogResponseDto.AuditLogResponseDtoBuilder auditLogResponseDto = AuditLogResponseDto.builder();

        auditLogResponseDto.action( auditLog.getAction() );
        auditLogResponseDto.entityId( auditLog.getEntityId() );
        auditLogResponseDto.entityName( auditLog.getEntityName() );
        auditLogResponseDto.id( auditLog.getId() );
        auditLogResponseDto.newState( auditLog.getNewState() );
        auditLogResponseDto.oldState( auditLog.getOldState() );
        auditLogResponseDto.performedBy( auditLog.getPerformedBy() );
        auditLogResponseDto.timestamp( auditLog.getTimestamp() );

        return auditLogResponseDto.build();
    }
}
