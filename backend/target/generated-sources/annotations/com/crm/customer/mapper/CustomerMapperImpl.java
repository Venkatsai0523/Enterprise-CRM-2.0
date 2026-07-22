package com.crm.customer.mapper;

import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.entity.CustomerAccount;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T01:55:21+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerAccountResponseDto toDto(CustomerAccount account) {
        if ( account == null ) {
            return null;
        }

        CustomerAccountResponseDto.CustomerAccountResponseDtoBuilder customerAccountResponseDto = CustomerAccountResponseDto.builder();

        customerAccountResponseDto.id( account.getId() );
        customerAccountResponseDto.accountName( account.getAccountName() );
        customerAccountResponseDto.domainName( account.getDomainName() );
        customerAccountResponseDto.primaryEmail( account.getPrimaryEmail() );
        customerAccountResponseDto.phone( account.getPhone() );
        customerAccountResponseDto.status( account.getStatus() );
        customerAccountResponseDto.createdAt( account.getCreatedAt() );
        customerAccountResponseDto.updatedAt( account.getUpdatedAt() );

        return customerAccountResponseDto.build();
    }
}
