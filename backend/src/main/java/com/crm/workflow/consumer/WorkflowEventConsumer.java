package com.crm.workflow.consumer;

import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.LeadApi;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.lead.api.event.LeadScoredEvent;
import com.crm.opportunity.api.event.DealWonEvent;
import com.crm.opportunity.api.event.DealLostEvent;
import com.crm.workflow.api.WorkflowApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventConsumer {

    private final WorkflowApi workflowApi;
    private final LeadApi leadApi;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_LEAD_SCORED,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-workflow"
    )
    public void consumeLeadScored(LeadScoredEvent event) {
        com.crm.infrastructure.tenant.TenantContext.setTenantId(event.getOrganizationId());
        try {
            log.info("Workflow received LeadScoredEvent for lead: {}", event.getLeadId());
            
            // Resolve assigned rep (using leadApi)
            UUID assignedRepId = null;
            Optional<LeadResponseDto> leadOpt = leadApi.findLeadById(event.getLeadId());
            if (leadOpt.isPresent()) {
                assignedRepId = leadOpt.get().getAssignedRepId();
            }

            Map<String, Object> context = new HashMap<>();
            context.put("entityId", event.getLeadId());
            context.put("entityType", "LEAD");
            context.put("email", event.getEmail());
            context.put("companyName", event.getCompanyName());
            context.put("score", event.getScore());
            context.put("assignedRepId", assignedRepId);

            workflowApi.evaluateAndExecute("LEAD_SCORED", context, event.getOrganizationId());
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_DEAL_WON,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-workflow"
    )
    public void consumeDealWon(DealWonEvent event) {
        com.crm.infrastructure.tenant.TenantContext.setTenantId(event.getOrganizationId());
        try {
            log.info("Workflow received DealWonEvent for opportunity: {}", event.getOpportunityId());

            Map<String, Object> context = new HashMap<>();
            context.put("entityId", event.getOpportunityId());
            context.put("entityType", "OPPORTUNITY");
            context.put("title", event.getTitle());
            context.put("amount", event.getAmount());
            context.put("assignedRepId", event.getLeadId()); // Default recipient fallback (lead ID or system admin)

            workflowApi.evaluateAndExecute("DEAL_WON", context, event.getOrganizationId());
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_DEAL_LOST,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-workflow"
    )
    public void consumeDealLost(DealLostEvent event) {
        com.crm.infrastructure.tenant.TenantContext.setTenantId(event.getOrganizationId());
        try {
            log.info("Workflow received DealLostEvent for opportunity: {}", event.getOpportunityId());

            Map<String, Object> context = new HashMap<>();
            context.put("entityId", event.getOpportunityId());
            context.put("entityType", "OPPORTUNITY");
            context.put("title", event.getTitle());
            context.put("amount", event.getAmount());
            context.put("lostReason", event.getLostReason());
            context.put("assignedRepId", event.getLeadId());

            workflowApi.evaluateAndExecute("DEAL_LOST", context, event.getOrganizationId());
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }
}
