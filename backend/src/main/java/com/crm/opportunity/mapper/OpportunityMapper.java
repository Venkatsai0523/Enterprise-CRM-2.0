package com.crm.opportunity.mapper;

import com.crm.opportunity.api.dto.OpportunityCreateDto;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.opportunity.api.dto.OpportunityUpdateDto;
import com.crm.opportunity.entity.Opportunity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OpportunityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "lostReason", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Opportunity toEntity(OpportunityCreateDto dto);

    OpportunityResponseDto toDto(Opportunity opportunity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leadId", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(OpportunityUpdateDto dto, @org.mapstruct.MappingTarget Opportunity entity);
}
