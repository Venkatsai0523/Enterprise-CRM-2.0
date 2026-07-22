package com.crm.organization.controller;

import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.api.dto.OrganizationUpdateDto;
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
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Tenant (organization) creation and lookup")
public class OrganizationController {

    private final OrganizationApi organizationApi;

    @GetMapping("/lookup")
    @Operation(summary = "Lookup organization by subdomain", description = "Retrieves public organization details by subdomain without authentication")
    public ResponseEntity<OrganizationResponseDto> lookupOrganization(
            @RequestParam String subdomain
    ) {
        return organizationApi.findOrganizationBySubdomain(subdomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new organization", description = "Provisions a new tenant organization with a unique subdomain")
    public ResponseEntity<OrganizationResponseDto> createOrganization(
            @Valid @RequestBody OrganizationCreateDto dto
    ) {
        OrganizationResponseDto created = organizationApi.createOrganization(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get organization by ID", description = "Retrieves organization details by its UUID")
    public ResponseEntity<OrganizationResponseDto> getOrganizationById(
            @PathVariable UUID id
    ) {
        return organizationApi.findOrganizationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "List all organizations", description = "Retrieves all registered organizations")
    public ResponseEntity<List<OrganizationResponseDto>> getAllOrganizations() {
        List<OrganizationResponseDto> orgs = organizationApi.getAllOrganizations();
        return ResponseEntity.ok(orgs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update organization details", description = "Updates an organization's configuration and status")
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationUpdateDto dto
    ) {
        OrganizationResponseDto updated = organizationApi.updateOrganization(id, dto);
        return ResponseEntity.ok(updated);
    }
}
