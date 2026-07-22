package com.crm.opportunity.api;

import com.crm.opportunity.api.dto.OpportunityResponseDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Published cross-domain interface for Opportunity domain.
 */
public interface OpportunityApi {

    Optional<OpportunityResponseDto> findOpportunityById(UUID opportunityId);

    boolean existsById(UUID opportunityId);

    java.util.List<OpportunityResponseDto> findAllOpportunities();
}
