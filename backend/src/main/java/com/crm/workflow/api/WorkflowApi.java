package com.crm.workflow.api;

import java.util.Map;
import java.util.UUID;

/**
 * Published cross-domain interface for Workflow rules engine.
 */
public interface WorkflowApi {

    /**
     * Evaluates all active workflow rules matching the given triggerEvent and executes their actions.
     *
     * @param triggerEvent The trigger event identifier (e.g. "LEAD_SCORED", "DEAL_WON", "DEAL_LOST")
     * @param context The evaluation context payload
     * @param organizationId The tenant organization context
     */
    void evaluateAndExecute(String triggerEvent, Map<String, Object> context, UUID organizationId);
}
