package com.crm.organization.controller;

import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.api.dto.OrganizationUpdateDto;
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
public class OrganizationController {

    private final OrganizationApi organizationApi;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> createOrganization(
            @Valid @RequestBody OrganizationCreateDto dto
    ) {
        OrganizationResponseDto created = organizationApi.createOrganization(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrganizationResponseDto> getOrganizationById(
            @PathVariable UUID id
    ) {
        return organizationApi.findOrganizationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<OrganizationResponseDto>> getAllOrganizations() {
        List<OrganizationResponseDto> orgs = organizationApi.getAllOrganizations();
        return ResponseEntity.ok(orgs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationUpdateDto dto
    ) {
        OrganizationResponseDto updated = organizationApi.updateOrganization(id, dto);
        return ResponseEntity.ok(updated);
    }
}
