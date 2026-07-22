package com.crm.workflow.mapper;

import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.entity.WorkflowRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkflowRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    WorkflowRule toEntity(WorkflowRuleCreateDto dto);

    WorkflowRuleResponseDto toDto(WorkflowRule rule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "triggerEvent", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntityFromDto(WorkflowRuleUpdateDto dto, @MappingTarget WorkflowRule entity);
}
