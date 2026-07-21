package com.crm.opportunity.mapper;

import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.entity.Opportunity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T00:13:00+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class OpportunityMapperImpl implements OpportunityMapper {

    @Override
    public Opportunity toEntity(OpportunityCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Opportunity.OpportunityBuilder opportunity = Opportunity.builder();

        opportunity.estimatedValue( dto.getEstimatedValue() );
        opportunity.leadId( dto.getLeadId() );
        opportunity.title( dto.getTitle() );

        return opportunity.build();
    }

    @Override
    public OpportunityResponseDto toDto(Opportunity opportunity) {
        if ( opportunity == null ) {
            return null;
        }

        OpportunityResponseDto.OpportunityResponseDtoBuilder opportunityResponseDto = OpportunityResponseDto.builder();

        opportunityResponseDto.closedAt( opportunity.getClosedAt() );
        opportunityResponseDto.createdAt( opportunity.getCreatedAt() );
        opportunityResponseDto.estimatedValue( opportunity.getEstimatedValue() );
        opportunityResponseDto.id( opportunity.getId() );
        opportunityResponseDto.leadId( opportunity.getLeadId() );
        opportunityResponseDto.lostReason( opportunity.getLostReason() );
        opportunityResponseDto.stage( opportunity.getStage() );
        opportunityResponseDto.title( opportunity.getTitle() );
        opportunityResponseDto.updatedAt( opportunity.getUpdatedAt() );

        return opportunityResponseDto.build();
    }
}
