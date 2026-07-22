package com.crm.audit.mapper;

import com.crm.audit.api.dto.AuditLogResponseDto;
import com.crm.audit.entity.AuditLog;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T20:23:30+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Override
    public AuditLogResponseDto toDto(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }

        AuditLogResponseDto.AuditLogResponseDtoBuilder auditLogResponseDto = AuditLogResponseDto.builder();

        auditLogResponseDto.id( auditLog.getId() );
        auditLogResponseDto.entityName( auditLog.getEntityName() );
        auditLogResponseDto.entityId( auditLog.getEntityId() );
        auditLogResponseDto.action( auditLog.getAction() );
        auditLogResponseDto.performedBy( auditLog.getPerformedBy() );
        auditLogResponseDto.oldState( auditLog.getOldState() );
        auditLogResponseDto.newState( auditLog.getNewState() );
        auditLogResponseDto.timestamp( auditLog.getTimestamp() );

        return auditLogResponseDto.build();
    }
}
