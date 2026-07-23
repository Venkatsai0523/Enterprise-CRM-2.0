package com.crm.lead.service;

import com.crm.identity.api.IdentityApi;
import com.crm.identity.api.dto.UserResponseDto;
import com.crm.lead.api.dto.LeadStatus;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.lead.entity.Lead;
import com.crm.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class LeadAssignmentService {

    private final LeadRepository leadRepository;
    private final IdentityApi identityApi;

    // Fallback static rep UUID for testing/demo when no reps registered
    private static final UUID FALLBACK_REP_ID = UUID.fromString("b0000000-0000-0000-0000-000000000002");

    @Transactional
    public Optional<LeadAssignedEvent> assignLead(UUID leadId) {
        Optional<Lead> leadOpt = leadRepository.findById(leadId);
        if (leadOpt.isEmpty()) {
            log.warn("Lead not found for auto-assignment: {}", leadId);
            return Optional.empty();
        }

        Lead lead = leadOpt.get();

        // Idempotency check: only assign if lead is in SCORED status
        if (lead.getStatus() != LeadStatus.SCORED) {
            log.info("Lead {} is in status {}, skipping auto-assignment (idempotent guard)", leadId, lead.getStatus());
            return Optional.empty();
        }

        // Determine target sales rep using IdentityApi
        UUID assignedRepId = resolveRepId();

        lead.setAssignedRepId(assignedRepId);
        lead.setStatus(LeadStatus.ASSIGNED);
        leadRepository.save(lead);

        log.info("Lead {} successfully auto-assigned to Rep {}", leadId, assignedRepId);

        return Optional.of(LeadAssignedEvent.builder()
                .leadId(lead.getId())
                .assignedRepId(assignedRepId)
                .score(lead.getScore())
                .organizationId(lead.getOrganizationId())
                .build());
    }

    private UUID resolveRepId() {
        // Query identity module via published IdentityApi interface for active ROLE_SALES_REP users in the tenant
        java.util.List<UserResponseDto> activeReps = identityApi.findActiveUsersByRole("ROLE_SALES_REP");
        
        if (activeReps.isEmpty()) {
            log.warn("No active Sales Representatives found for tenant context auto-assignment. Falling back to {}", FALLBACK_REP_ID);
            return FALLBACK_REP_ID;
        }

        UUID leastLoadedRepId = null;
        long minLeadCount = Long.MAX_VALUE;

        for (UserResponseDto rep : activeReps) {
            long currentCount = leadRepository.countByAssignedRepId(rep.getId());
            if (currentCount < minLeadCount) {
                minLeadCount = currentCount;
                leastLoadedRepId = rep.getId();
            }
        }

        log.info("Resolved least-loaded Sales Rep ID: {} with {} active leads", leastLoadedRepId, minLeadCount);
        return leastLoadedRepId != null ? leastLoadedRepId : FALLBACK_REP_ID;
    }
}
