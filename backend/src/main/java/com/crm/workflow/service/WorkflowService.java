package com.crm.workflow.service;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.workflow.api.WorkflowApi;
import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.entity.WorkflowRule;
import com.crm.workflow.engine.WorkflowEngine;
import com.crm.workflow.mapper.WorkflowRuleMapper;
import com.crm.workflow.repository.WorkflowRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class WorkflowService implements WorkflowApi {

    private final WorkflowRuleRepository ruleRepository;
    private final WorkflowRuleMapper ruleMapper;
    private final WorkflowEngine workflowEngine;

    @Override
    @Transactional
    public void evaluateAndExecute(String triggerEvent, Map<String, Object> context, UUID organizationId) {
        log.info("Evaluating workflow rules for event: '{}' in tenant: '{}'", triggerEvent, organizationId);
        List<WorkflowRule> activeRules = ruleRepository.findByTriggerEventAndActiveTrue(triggerEvent);
        
        for (WorkflowRule rule : activeRules) {
            log.debug("Evaluating rule: '{}'", rule.getName());
            if (workflowEngine.evaluateConditions(rule.getConditionsJson(), context)) {
                log.info("Conditions matched for rule '{}'. Executing actions.", rule.getName());
                workflowEngine.executeActions(rule.getActionsJson(), context, organizationId);
            } else {
                log.debug("Conditions did not match for rule '{}'. Skipping.", rule.getName());
            }
        }
    }

    @Transactional
    public WorkflowRuleResponseDto createRule(WorkflowRuleCreateDto dto) {
        WorkflowRule rule = ruleMapper.toEntity(dto);
        // Tenant is automatically resolved and populated by Hibernate @TenantId
        WorkflowRule saved = ruleRepository.save(rule);
        log.info("Created workflow rule: {}", saved.getName());
        return ruleMapper.toDto(saved);
    }

    @Transactional
    public WorkflowRuleResponseDto updateRule(UUID id, WorkflowRuleUpdateDto dto) {
        WorkflowRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow rule not found with ID: " + id));

        ruleMapper.updateEntityFromDto(dto, rule);
        WorkflowRule saved = ruleRepository.save(rule);
        log.info("Updated workflow rule: {}", saved.getName());
        return ruleMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<WorkflowRuleResponseDto> getRuleById(UUID id) {
        return ruleRepository.findById(id).map(ruleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<WorkflowRuleResponseDto> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowRuleResponseDto> getRulesByEvent(String triggerEvent) {
        return ruleRepository.findByTriggerEventAndActiveTrue(triggerEvent).stream()
                .map(ruleMapper::toDto)
                .collect(Collectors.toList());
    }
}
