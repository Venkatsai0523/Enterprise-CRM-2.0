package com.crm.organization.mapper;

import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.entity.Organization;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T13:58:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public OrganizationResponseDto toDto(Organization organization) {
        if ( organization == null ) {
            return null;
        }

        OrganizationResponseDto.OrganizationResponseDtoBuilder organizationResponseDto = OrganizationResponseDto.builder();

        organizationResponseDto.active( organization.isActive() );
        organizationResponseDto.createdAt( organization.getCreatedAt() );
        organizationResponseDto.id( organization.getId() );
        organizationResponseDto.name( organization.getName() );
        organizationResponseDto.subdomain( organization.getSubdomain() );
        organizationResponseDto.updatedAt( organization.getUpdatedAt() );

        return organizationResponseDto.build();
    }
}
