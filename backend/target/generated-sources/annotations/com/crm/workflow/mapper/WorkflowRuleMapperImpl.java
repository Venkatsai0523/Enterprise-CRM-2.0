package com.crm.workflow.mapper;

import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.entity.WorkflowRule;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T13:42:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class WorkflowRuleMapperImpl implements WorkflowRuleMapper {

    @Override
    public WorkflowRule toEntity(WorkflowRuleCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        WorkflowRule.WorkflowRuleBuilder workflowRule = WorkflowRule.builder();

        workflowRule.name( dto.getName() );
        workflowRule.description( dto.getDescription() );
        workflowRule.triggerEvent( dto.getTriggerEvent() );
        workflowRule.conditionsJson( dto.getConditionsJson() );
        workflowRule.actionsJson( dto.getActionsJson() );

        return workflowRule.build();
    }

    @Override
    public WorkflowRuleResponseDto toDto(WorkflowRule rule) {
        if ( rule == null ) {
            return null;
        }

        WorkflowRuleResponseDto.WorkflowRuleResponseDtoBuilder workflowRuleResponseDto = WorkflowRuleResponseDto.builder();

        workflowRuleResponseDto.id( rule.getId() );
        workflowRuleResponseDto.name( rule.getName() );
        workflowRuleResponseDto.description( rule.getDescription() );
        workflowRuleResponseDto.triggerEvent( rule.getTriggerEvent() );
        workflowRuleResponseDto.conditionsJson( rule.getConditionsJson() );
        workflowRuleResponseDto.actionsJson( rule.getActionsJson() );
        workflowRuleResponseDto.active( rule.isActive() );
        workflowRuleResponseDto.organizationId( rule.getOrganizationId() );

        return workflowRuleResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(WorkflowRuleUpdateDto dto, WorkflowRule entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setDescription( dto.getDescription() );
        entity.setConditionsJson( dto.getConditionsJson() );
        entity.setActionsJson( dto.getActionsJson() );
        entity.setActive( dto.isActive() );
    }
}
