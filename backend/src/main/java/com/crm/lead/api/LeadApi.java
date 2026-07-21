package com.crm.lead.api;

import com.crm.lead.api.dto.LeadResponseDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Published cross-domain interface for Lead domain.
 */
public interface LeadApi {

    Optional<LeadResponseDto> findLeadById(UUID leadId);

    boolean updateStatus(UUID leadId, String newStatus);
}
