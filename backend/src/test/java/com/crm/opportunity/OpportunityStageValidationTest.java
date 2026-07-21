package com.crm.opportunity;

import com.crm.common.exception.BadRequestException;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.opportunity.service.OpportunityStageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpportunityStageValidationTest {

    private OpportunityStageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OpportunityStageValidator();
    }

    @Test
    @DisplayName("Valid Transitions: Forward lifecycle transitions must pass validation")
    void validStageTransitions() {
        assertThatCode(() -> validator.validateTransition(OpportunityStage.PROSPECTING, OpportunityStage.PROPOSAL, null))
                .doesNotThrowAnyException();

        assertThatCode(() -> validator.validateTransition(OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION, null))
                .doesNotThrowAnyException();

        assertThatCode(() -> validator.validateTransition(OpportunityStage.NEGOTIATION, OpportunityStage.WON, null))
                .doesNotThrowAnyException();

        assertThatCode(() -> validator.validateTransition(OpportunityStage.PROSPECTING, OpportunityStage.LOST, "Price too high"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Invalid Transitions: Transitioning backwards from WON/LOST must be rejected")
    void invalidTerminalTransitions() {
        assertThatThrownBy(() -> validator.validateTransition(OpportunityStage.WON, OpportunityStage.PROSPECTING, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid stage transition from 'WON' to 'PROSPECTING'");

        assertThatThrownBy(() -> validator.validateTransition(OpportunityStage.LOST, OpportunityStage.PROPOSAL, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid stage transition from 'LOST' to 'PROPOSAL'");
    }

    @Test
    @DisplayName("Invalid Transitions: Skipping stages backward (NEGOTIATION to PROSPECTING) must be rejected")
    void invalidBackwardTransitions() {
        assertThatThrownBy(() -> validator.validateTransition(OpportunityStage.NEGOTIATION, OpportunityStage.PROSPECTING, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid stage transition");
    }

    @Test
    @DisplayName("Lost Reason Mandate: Marking as LOST requires a non-blank lostReason")
    void missingLostReasonValidation() {
        assertThatThrownBy(() -> validator.validateTransition(OpportunityStage.PROSPECTING, OpportunityStage.LOST, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("A valid 'lostReason' is required");

        assertThatThrownBy(() -> validator.validateTransition(OpportunityStage.PROSPECTING, OpportunityStage.LOST, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("A valid 'lostReason' is required");
    }
}
