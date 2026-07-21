package com.crm.lead.mapper;

import com.crm.lead.api.dto.LeadCreateDto;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.entity.Lead;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T00:13:00+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class LeadMapperImpl implements LeadMapper {

    @Override
    public Lead toEntity(LeadCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Lead.LeadBuilder lead = Lead.builder();

        lead.companyName( dto.getCompanyName() );
        lead.companySize( dto.getCompanySize() );
        lead.email( dto.getEmail() );
        lead.firstName( dto.getFirstName() );
        lead.lastName( dto.getLastName() );
        lead.leadSource( dto.getLeadSource() );
        lead.phone( dto.getPhone() );

        return lead.build();
    }

    @Override
    public LeadResponseDto toDto(Lead lead) {
        if ( lead == null ) {
            return null;
        }

        LeadResponseDto.LeadResponseDtoBuilder leadResponseDto = LeadResponseDto.builder();

        leadResponseDto.assignedRepId( lead.getAssignedRepId() );
        leadResponseDto.companyName( lead.getCompanyName() );
        leadResponseDto.companySize( lead.getCompanySize() );
        leadResponseDto.createdAt( lead.getCreatedAt() );
        leadResponseDto.email( lead.getEmail() );
        leadResponseDto.firstName( lead.getFirstName() );
        leadResponseDto.id( lead.getId() );
        leadResponseDto.lastName( lead.getLastName() );
        leadResponseDto.leadSource( lead.getLeadSource() );
        leadResponseDto.phone( lead.getPhone() );
        leadResponseDto.score( lead.getScore() );
        leadResponseDto.status( lead.getStatus() );
        leadResponseDto.updatedAt( lead.getUpdatedAt() );

        return leadResponseDto.build();
    }
}
