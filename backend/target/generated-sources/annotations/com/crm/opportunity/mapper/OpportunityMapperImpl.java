package com.crm.opportunity.mapper;

import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.api.dto.OpportunityUpdateDto;
import com.crm.opportunity.entity.Opportunity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T01:55:20+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class OpportunityMapperImpl implements OpportunityMapper {

    @Override
    public Opportunity toEntity(OpportunityCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Opportunity.OpportunityBuilder opportunity = Opportunity.builder();

        opportunity.title( dto.getTitle() );
        opportunity.leadId( dto.getLeadId() );
        opportunity.estimatedValue( dto.getEstimatedValue() );

        return opportunity.build();
    }

    @Override
    public OpportunityResponseDto toDto(Opportunity opportunity) {
        if ( opportunity == null ) {
            return null;
        }

        OpportunityResponseDto.OpportunityResponseDtoBuilder opportunityResponseDto = OpportunityResponseDto.builder();

        opportunityResponseDto.id( opportunity.getId() );
        opportunityResponseDto.title( opportunity.getTitle() );
        opportunityResponseDto.leadId( opportunity.getLeadId() );
        opportunityResponseDto.estimatedValue( opportunity.getEstimatedValue() );
        opportunityResponseDto.stage( opportunity.getStage() );
        opportunityResponseDto.lostReason( opportunity.getLostReason() );
        opportunityResponseDto.closedAt( opportunity.getClosedAt() );
        opportunityResponseDto.createdAt( opportunity.getCreatedAt() );
        opportunityResponseDto.updatedAt( opportunity.getUpdatedAt() );

        return opportunityResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(OpportunityUpdateDto dto, Opportunity entity) {
        if ( dto == null ) {
            return;
        }

        entity.setTitle( dto.getTitle() );
        entity.setEstimatedValue( dto.getEstimatedValue() );
        entity.setStage( dto.getStage() );
        entity.setLostReason( dto.getLostReason() );
    }
}
