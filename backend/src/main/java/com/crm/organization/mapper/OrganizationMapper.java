package com.crm.organization.mapper;

import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.entity.Organization;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationResponseDto toDto(Organization organization);
}
