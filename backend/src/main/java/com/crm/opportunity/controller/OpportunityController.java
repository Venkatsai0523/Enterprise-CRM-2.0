package com.crm.opportunity.controller;

import com.crm.opportunity.api.dto.*;
import com.crm.opportunity.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
@Tag(name = "Opportunity Pipeline", description = "Endpoints for proposal, negotiation, stage transitions, and lost deal analysis")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Create opportunity", description = "Creates a new opportunity linked to a qualified lead starting in PROSPECTING stage")
    public ResponseEntity<OpportunityResponseDto> createOpportunity(@Valid @RequestBody OpportunityCreateDto dto) {
        OpportunityResponseDto response = opportunityService.createOpportunity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Transition opportunity stage", description = "Executes state machine transition. Validates allowed stage transitions and triggers DealWon/DealLost Kafka events")
    public ResponseEntity<OpportunityResponseDto> updateStage(
            @PathVariable UUID id,
            @Valid @RequestBody OpportunityStageUpdateDto dto
    ) {
        OpportunityResponseDto response = opportunityService.updateStage(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Get opportunity by ID", description = "Retrieves full opportunity details including current stage and lost reason")
    public ResponseEntity<OpportunityResponseDto> getOpportunityById(@PathVariable UUID id) {
        OpportunityResponseDto response = opportunityService.getOpportunityById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Search and filter opportunities", description = "Retrieves paginated opportunities filtered by stage and lead ID")
    public ResponseEntity<Page<OpportunityResponseDto>> getOpportunities(
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) UUID leadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OpportunityResponseDto> response = opportunityService.getOpportunitiesWithFilters(stage, leadId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/lost-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lost deal analysis report", description = "Aggregates lost deal metrics grouped by lost reason")
    public ResponseEntity<List<LostAnalysisDto>> getLostAnalysis() {
        List<LostAnalysisDto> response = opportunityService.getLostAnalysis();
        return ResponseEntity.ok(response);
    }
}
