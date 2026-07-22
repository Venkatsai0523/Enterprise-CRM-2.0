package com.crm.notification.consumer;

import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.notification.entity.Notification;
import com.crm.notification.repository.NotificationRepository;
import com.crm.opportunity.api.event.DealWonEvent;
import com.crm.task.api.event.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final com.crm.organization.api.OrganizationApi organizationApi;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_LEAD_ASSIGNED,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-notifications"
    )
    public void consumeLeadAssigned(LeadAssignedEvent event) {
        UUID orgId = event.getOrganizationId() != null ? event.getOrganizationId() : com.crm.infrastructure.tenant.TenantContextResolver.DEFAULT_ORG_ID;
        if (!organizationApi.existsById(orgId)) {
            log.warn("Organization with ID {} does not exist. Skipping LEAD_ASSIGNED notification.", orgId);
            return;
        }

        com.crm.infrastructure.tenant.TenantContext.setTenantId(orgId);
        try {
            log.info("NotificationConsumer received LeadAssignedEvent for lead ID: {}", event.getLeadId());

            if (event.getAssignedRepId() == null) {
                return;
            }

            Notification notification = Notification.builder()
                    .recipientId(event.getAssignedRepId())
                    .type("LEAD_ASSIGNED")
                    .message(String.format("A new lead (ID: %s, Score: %d) has been assigned to you.",
                            event.getLeadId(), event.getScore()))
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved LEAD_ASSIGNED notification for rep ID: {}", event.getAssignedRepId());
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_DEAL_WON,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-notifications"
    )
    public void consumeDealWon(DealWonEvent event) {
        UUID orgId = event.getOrganizationId() != null ? event.getOrganizationId() : com.crm.infrastructure.tenant.TenantContextResolver.DEFAULT_ORG_ID;
        if (!organizationApi.existsById(orgId)) {
            log.warn("Organization with ID {} does not exist. Skipping DEAL_WON notification.", orgId);
            return;
        }

        com.crm.infrastructure.tenant.TenantContext.setTenantId(orgId);
        try {
            log.info("NotificationConsumer received DealWonEvent for opportunity ID: {}", event.getOpportunityId());

            // Default recipient is the lead's owner / admin system recipient
            UUID recipientId = event.getLeadId();

            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .type("DEAL_WON")
                    .message(String.format("Deal '%s' (Opportunity ID: %s) has been successfully WON for $%s!",
                            event.getTitle(), event.getOpportunityId(), event.getAmount()))
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved DEAL_WON notification for recipient ID: {}", recipientId);
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_TASK_ASSIGNED,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-notifications"
    )
    public void consumeTaskAssigned(TaskAssignedEvent event) {
        UUID orgId = event.getOrganizationId() != null ? event.getOrganizationId() : com.crm.infrastructure.tenant.TenantContextResolver.DEFAULT_ORG_ID;
        if (!organizationApi.existsById(orgId)) {
            log.warn("Organization with ID {} does not exist. Skipping TASK_ASSIGNED notification.", orgId);
            return;
        }

        com.crm.infrastructure.tenant.TenantContext.setTenantId(orgId);
        try {
            log.info("NotificationConsumer received TaskAssignedEvent for task ID: {}", event.getTaskId());

            if (event.getAssignedTo() == null) {
                return;
            }

            Notification notification = Notification.builder()
                    .recipientId(event.getAssignedTo())
                    .type("TASK_ASSIGNED")
                    .message(String.format("A new task has been assigned to you: '%s' (Due: %s).",
                            event.getTitle(), event.getDueDate() != null ? event.getDueDate().toString() : "No due date"))
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved TASK_ASSIGNED notification for recipient ID: {}", event.getAssignedTo());
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }
}
