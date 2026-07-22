package com.crm.workflow.controller;

import com.crm.workflow.api.dto.WorkflowRuleCreateDto;
import com.crm.workflow.api.dto.WorkflowRuleResponseDto;
import com.crm.workflow.api.dto.WorkflowRuleUpdateDto;
import com.crm.workflow.service.WorkflowService;
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
public class WorkflowRuleController {

    private final WorkflowService workflowService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WorkflowRuleResponseDto> createRule(
            @Valid @RequestBody WorkflowRuleCreateDto dto
    ) {
        WorkflowRuleResponseDto created = workflowService.createRule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<WorkflowRuleResponseDto> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody WorkflowRuleUpdateDto dto
    ) {
        WorkflowRuleResponseDto updated = workflowService.updateRule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<WorkflowRuleResponseDto> getRuleById(
            @PathVariable UUID id
    ) {
        return workflowService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
    public ResponseEntity<List<WorkflowRuleResponseDto>> getAllRules() {
        List<WorkflowRuleResponseDto> rules = workflowService.getAllRules();
        return ResponseEntity.ok(rules);
    }
}
