package com.crm.customer.mapper;

import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.entity.CustomerAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerAccountResponseDto toDto(CustomerAccount account);
}
