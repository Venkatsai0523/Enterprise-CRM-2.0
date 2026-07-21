package com.crm.customer.api;

import com.crm.customer.api.dto.CustomerAccountResponseDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Published cross-domain interface for Customer domain.
 */
public interface CustomerApi {

    Optional<CustomerAccountResponseDto> findCustomerById(UUID customerId);

    Optional<CustomerAccountResponseDto> findCustomerByDomain(String domainName);

    boolean existsById(UUID customerId);
}
