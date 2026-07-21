package com.crm.notification.consumer;

import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.notification.entity.Notification;
import com.crm.notification.repository.NotificationRepository;
import com.crm.opportunity.api.event.DealWonEvent;
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

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_LEAD_ASSIGNED,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-notifications"
    )
    public void consumeLeadAssigned(LeadAssignedEvent event) {
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
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_DEAL_WON,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}-notifications"
    )
    public void consumeDealWon(DealWonEvent event) {
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
    }
}
