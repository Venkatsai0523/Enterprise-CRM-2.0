package com.crm.workflow.controller;

import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows/rules")
@RequiredArgsConstructor
@Tag(name = "Workflow Automation", description = "Configurable trigger/condition/action rules")
public class WorkflowRuleController {

    private final WorkflowService workflowService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create workflow rule", description = "Registers a new trigger rule with structured JSON conditions and automated actions")
    public ResponseEntity<WorkflowRuleResponseDto> createRule(
            @Valid @RequestBody WorkflowRuleCreateDto dto
    ) {
        WorkflowRuleResponseDto created = workflowService.createRule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update workflow rule", description = "Updates rule metadata, conditions, or actions")
    public ResponseEntity<WorkflowRuleResponseDto> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowRuleUpdateDto dto
    ) {
        WorkflowRuleResponseDto updated = workflowService.updateRule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    @Operation(summary = "Get workflow rule by ID", description = "Retrieves workflow rule properties by UUID")
    public ResponseEntity<WorkflowRuleResponseDto> getRuleById(
            @PathVariable UUID id
    ) {
        return workflowService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    @Operation(summary = "List all workflow rules", description = "Retrieves all workflow rules registered in the user's organization")
    public ResponseEntity<List<WorkflowRuleResponseDto>> getAllRules() {
        List<WorkflowRuleResponseDto> rules = workflowService.getAllRules();
        return ResponseEntity.ok(rules);
    }
}
