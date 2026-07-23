package com.crm.workflow.mapper;

import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.entity.WorkflowRule;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T13:58:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class WorkflowRuleMapperImpl implements WorkflowRuleMapper {

    @Override
    public WorkflowRule toEntity(WorkflowRuleCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        WorkflowRule.WorkflowRuleBuilder workflowRule = WorkflowRule.builder();

        workflowRule.actionsJson( dto.getActionsJson() );
        workflowRule.conditionsJson( dto.getConditionsJson() );
        workflowRule.description( dto.getDescription() );
        workflowRule.name( dto.getName() );
        workflowRule.triggerEvent( dto.getTriggerEvent() );

        return workflowRule.build();
    }

    @Override
    public WorkflowRuleResponseDto toDto(WorkflowRule rule) {
        if ( rule == null ) {
            return null;
        }

        WorkflowRuleResponseDto.WorkflowRuleResponseDtoBuilder workflowRuleResponseDto = WorkflowRuleResponseDto.builder();

        workflowRuleResponseDto.actionsJson( rule.getActionsJson() );
        workflowRuleResponseDto.active( rule.isActive() );
        workflowRuleResponseDto.conditionsJson( rule.getConditionsJson() );
        workflowRuleResponseDto.description( rule.getDescription() );
        workflowRuleResponseDto.id( rule.getId() );
        workflowRuleResponseDto.name( rule.getName() );
        workflowRuleResponseDto.organizationId( rule.getOrganizationId() );
        workflowRuleResponseDto.triggerEvent( rule.getTriggerEvent() );

        return workflowRuleResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(WorkflowRuleUpdateDto dto, WorkflowRule entity) {
        if ( dto == null ) {
            return;
        }

        entity.setActionsJson( dto.getActionsJson() );
        entity.setActive( dto.isActive() );
        entity.setConditionsJson( dto.getConditionsJson() );
        entity.setDescription( dto.getDescription() );
        entity.setName( dto.getName() );
    }
}
