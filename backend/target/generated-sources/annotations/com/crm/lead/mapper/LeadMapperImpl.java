package com.crm.lead.mapper;

import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.api.dto.LeadUpdateDto;
import com.crm.lead.entity.Lead;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T01:55:21+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class LeadMapperImpl implements LeadMapper {

    @Override
    public Lead toEntity(LeadCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Lead.LeadBuilder lead = Lead.builder();

        lead.firstName( dto.getFirstName() );
        lead.lastName( dto.getLastName() );
        lead.email( dto.getEmail() );
        lead.phone( dto.getPhone() );
        lead.companyName( dto.getCompanyName() );
        lead.companySize( dto.getCompanySize() );
        lead.leadSource( dto.getLeadSource() );

        return lead.build();
    }

    @Override
    public LeadResponseDto toDto(Lead lead) {
        if ( lead == null ) {
            return null;
        }

        LeadResponseDto.LeadResponseDtoBuilder leadResponseDto = LeadResponseDto.builder();

        leadResponseDto.id( lead.getId() );
        leadResponseDto.firstName( lead.getFirstName() );
        leadResponseDto.lastName( lead.getLastName() );
        leadResponseDto.email( lead.getEmail() );
        leadResponseDto.phone( lead.getPhone() );
        leadResponseDto.companyName( lead.getCompanyName() );
        leadResponseDto.companySize( lead.getCompanySize() );
        leadResponseDto.leadSource( lead.getLeadSource() );
        leadResponseDto.status( lead.getStatus() );
        leadResponseDto.score( lead.getScore() );
        leadResponseDto.assignedRepId( lead.getAssignedRepId() );
        leadResponseDto.createdAt( lead.getCreatedAt() );
        leadResponseDto.updatedAt( lead.getUpdatedAt() );

        return leadResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(LeadUpdateDto dto, Lead entity) {
        if ( dto == null ) {
            return;
        }

        entity.setFirstName( dto.getFirstName() );
        entity.setLastName( dto.getLastName() );
        entity.setEmail( dto.getEmail() );
        entity.setPhone( dto.getPhone() );
        entity.setCompanyName( dto.getCompanyName() );
        entity.setCompanySize( dto.getCompanySize() );
        entity.setLeadSource( dto.getLeadSource() );
        entity.setStatus( dto.getStatus() );
    }
}
