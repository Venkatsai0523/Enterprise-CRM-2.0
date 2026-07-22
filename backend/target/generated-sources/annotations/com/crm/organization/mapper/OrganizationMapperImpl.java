package com.crm.organization.mapper;

import com.crm.organization.api.dto.OrganizationResponseDto;
import com.crm.organization.entity.Organization;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T13:42:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class OrganizationMapperImpl implements OrganizationMapper {

    @Override
    public OrganizationResponseDto toDto(Organization organization) {
        if ( organization == null ) {
            return null;
        }

        OrganizationResponseDto.OrganizationResponseDtoBuilder organizationResponseDto = OrganizationResponseDto.builder();

        organizationResponseDto.id( organization.getId() );
        organizationResponseDto.name( organization.getName() );
        organizationResponseDto.subdomain( organization.getSubdomain() );
        organizationResponseDto.active( organization.isActive() );
        organizationResponseDto.createdAt( organization.getCreatedAt() );
        organizationResponseDto.updatedAt( organization.getUpdatedAt() );

        return organizationResponseDto.build();
    }
}
