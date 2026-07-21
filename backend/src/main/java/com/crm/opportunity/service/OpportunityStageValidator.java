package com.crm.opportunity.service;

import com.crm.common.exception.BadRequestException;
import com.crm.opportunity.api.dto.OpportunityStage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OpportunityStageValidator {

    private static final Map<OpportunityStage, Set<OpportunityStage>> VALID_TRANSITIONS = Map.of(
            OpportunityStage.PROSPECTING, Set.of(OpportunityStage.PROPOSAL, OpportunityStage.LOST),
            OpportunityStage.PROPOSAL, Set.of(OpportunityStage.NEGOTIATION, OpportunityStage.WON, OpportunityStage.LOST),
            OpportunityStage.NEGOTIATION, Set.of(OpportunityStage.WON, OpportunityStage.LOST),
            OpportunityStage.WON, Set.of(),  // Terminal state
            OpportunityStage.LOST, Set.of()   // Terminal state
    );

    public void validateTransition(OpportunityStage currentStage, OpportunityStage targetStage, String lostReason) {
        if (currentStage == targetStage) {
            return; // No-op transition
        }

        Set<OpportunityStage> allowed = VALID_TRANSITIONS.getOrDefault(currentStage, Set.of());
        if (!allowed.contains(targetStage)) {
            throw new BadRequestException(String.format(
                    "Invalid stage transition from '%s' to '%s'. Allowed transitions: %s",
                    currentStage, targetStage, allowed
            ));
        }

        if (targetStage == OpportunityStage.LOST && (lostReason == null || lostReason.isBlank())) {
            throw new BadRequestException("A valid 'lostReason' is required when marking an opportunity as LOST");
        }
    }
}
