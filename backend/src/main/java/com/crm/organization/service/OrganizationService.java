package com.crm.organization.service;

import com.crm.common.exception.BadRequestException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.organization.api.OrganizationApi;
import com.crm.organization.api.dto.OrganizationCreateDto;
import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.api.dto.OrganizationUpdateDto;
import com.crm.organization.entity.Organization;
import com.crm.organization.mapper.OrganizationMapper;
import com.crm.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class OrganizationService implements OrganizationApi {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationResponseDto> findOrganizationById(UUID id) {
        return organizationRepository.findById(id).map(organizationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationResponseDto> findOrganizationBySubdomain(String subdomain) {
        return organizationRepository.findBySubdomain(subdomain).map(organizationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return organizationRepository.existsById(id);
    }

    @Override
    @Transactional
    public OrganizationResponseDto createOrganization(OrganizationCreateDto dto) {
        if (organizationRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Organization name already exists: " + dto.getName());
        }
        if (organizationRepository.existsBySubdomain(dto.getSubdomain())) {
            throw new BadRequestException("Organization subdomain already exists: " + dto.getSubdomain());
        }

        Organization organization = Organization.builder()
                .name(dto.getName())
                .subdomain(dto.getSubdomain())
                .active(true)
                .build();

        Organization saved = organizationRepository.save(organization);
        log.info("Created organization: {} with subdomain: {}", saved.getName(), saved.getSubdomain());
        return organizationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrganizationResponseDto updateOrganization(UUID id, OrganizationUpdateDto dto) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        organization.setName(dto.getName());
        organization.setActive(dto.isActive());

        Organization saved = organizationRepository.save(organization);
        log.info("Updated organization: {}", saved.getName());
        return organizationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponseDto> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());
    }
}
