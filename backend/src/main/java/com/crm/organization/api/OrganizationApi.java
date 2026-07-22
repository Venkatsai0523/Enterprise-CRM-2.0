package com.crm.organization.api;

import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.api.dto.OrganizationUpdateDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationApi {
    Optional<OrganizationResponseDto> findOrganizationById(UUID id);
    Optional<OrganizationResponseDto> findOrganizationBySubdomain(String subdomain);
    boolean existsById(UUID id);
    OrganizationResponseDto createOrganization(OrganizationCreateDto dto);
    OrganizationResponseDto updateOrganization(UUID id, OrganizationUpdateDto dto);
    List<OrganizationResponseDto> getAllOrganizations();
}
