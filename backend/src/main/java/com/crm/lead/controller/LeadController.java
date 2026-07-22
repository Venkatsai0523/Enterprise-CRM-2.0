package com.crm.lead.controller;

import com.crm.lead.api.dto.*;
import com.crm.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Lead Intelligence & Intake", description = "Endpoints for lead intake, synchronous scoring, and paginated lead discovery")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Intake and score a new lead", description = "Calculates a 0-100 weighted lead score synchronously and triggers asynchronous Kafka auto-assignment")
    public ResponseEntity<LeadResponseDto> intakeLead(@Valid @RequestBody LeadCreateDto dto) {
        LeadResponseDto response = leadService.createAndScoreLead(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Get lead by ID", description = "Retrieves full lead details by ID including current status, score, and assigned sales rep")
    public ResponseEntity<LeadResponseDto> getLeadById(@PathVariable UUID id) {
        LeadResponseDto response = leadService.getLeadById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Search and filter leads", description = "Retrieves paginated leads filtered by status and score range. Scoped implicitly to the caller's organizationId. Max page size: 100.")
    public ResponseEntity<Page<LeadResponseDto>> getLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Max(value = 100, message = "Page size must not exceed 100") @RequestParam(defaultValue = "10") int size
    ) {
        Page<LeadResponseDto> response = leadService.getLeadsWithFilters(status, minScore, maxScore, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Update lead details", description = "Updates an existing lead's fields by ID. Scoped to the caller's organization.")
    public ResponseEntity<LeadResponseDto> updateLead(
            @PathVariable UUID id,
            @Valid @RequestBody LeadUpdateDto dto
    ) {
        LeadResponseDto response = leadService.updateLead(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Soft delete a lead", description = "Soft deletes a lead by ID. Accessible by Admins and Managers.")
    public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}
