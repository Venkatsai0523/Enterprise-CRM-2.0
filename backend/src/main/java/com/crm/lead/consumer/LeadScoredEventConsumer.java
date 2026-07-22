package com.crm.lead.consumer;

import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.lead.api.event.LeadScoredEvent;
import com.crm.lead.service.LeadAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadScoredEventConsumer {

    private final LeadAssignmentService leadAssignmentService;
    private final KafkaEventPublisher eventPublisher;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_LEAD_SCORED,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}"
    )
    public void consumeLeadScoredEvent(LeadScoredEvent event) {
        com.crm.infrastructure.tenant.TenantContext.setTenantId(event.getOrganizationId());
        try {
            log.info("Received LeadScoredEvent for lead ID: {}, score: {}", event.getLeadId(), event.getScore());

            // Delegate to LeadAssignmentService (includes idempotent status check)
            Optional<LeadAssignedEvent> assignedEventOpt = leadAssignmentService.assignLead(event.getLeadId());

            assignedEventOpt.ifPresent(assignedEvent -> {
                log.info("Publishing LeadAssignedEvent for lead ID: {} assigned to Rep ID: {}",
                        assignedEvent.getLeadId(), assignedEvent.getAssignedRepId());
                eventPublisher.publish(
                        KafkaTopicConfig.TOPIC_LEAD_ASSIGNED,
                        assignedEvent.getLeadId().toString(),
                        assignedEvent
                );
            });
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }
}
