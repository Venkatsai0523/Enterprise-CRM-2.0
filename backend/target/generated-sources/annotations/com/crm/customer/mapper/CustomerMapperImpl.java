package com.crm.customer.mapper;

import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.entity.CustomerAccount;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T00:13:00+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerAccountResponseDto toDto(CustomerAccount account) {
        if ( account == null ) {
            return null;
        }

        CustomerAccountResponseDto.CustomerAccountResponseDtoBuilder customerAccountResponseDto = CustomerAccountResponseDto.builder();

        customerAccountResponseDto.accountName( account.getAccountName() );
        customerAccountResponseDto.createdAt( account.getCreatedAt() );
        customerAccountResponseDto.domainName( account.getDomainName() );
        customerAccountResponseDto.id( account.getId() );
        customerAccountResponseDto.phone( account.getPhone() );
        customerAccountResponseDto.primaryEmail( account.getPrimaryEmail() );
        customerAccountResponseDto.status( account.getStatus() );
        customerAccountResponseDto.updatedAt( account.getUpdatedAt() );

        return customerAccountResponseDto.build();
    }
}
