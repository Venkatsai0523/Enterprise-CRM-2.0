package com.crm.customer.api.dto;

import com.crm.opportunity.api.dto.OpportunityResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer360ResponseDto {

    private UUID id;
    private String accountName;
    private String domainName;
    private String primaryEmail;
    private String phone;
    private CustomerAccountStatus status;
    private BigDecimal totalLifetimeValue;
    private int opportunityCount;
    private List<OpportunityResponseDto> linkedOpportunities;
    private Instant createdAt;
    private Instant updatedAt;
}
