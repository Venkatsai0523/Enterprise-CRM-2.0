package com.crm.customer.consumer;

import com.crm.customer.entity.CustomerAccount;
import com.crm.customer.entity.CustomerOpportunityLink;
import com.crm.customer.repository.CustomerOpportunityLinkRepository;
import com.crm.customer.service.CustomerDedupService;
import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.LeadApi;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.opportunity.api.event.DealWonEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class DealWonConsumer {

    private final CustomerDedupService customerDedupService;
    private final CustomerOpportunityLinkRepository linkRepository;
    private final LeadApi leadApi;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_DEAL_WON,
            groupId = "${spring.kafka.consumer.group-id:nexus-crm-group}"
    )
    @Transactional
    public void consumeDealWonEvent(DealWonEvent event) {
        log.info("Received DealWonEvent for opportunity ID: {}, lead ID: {}", event.getOpportunityId(), event.getLeadId());

        if (linkRepository.existsByOpportunityId(event.getOpportunityId())) {
            log.info("Opportunity ID {} is already linked to a Customer Account, skipping (idempotent guard)", event.getOpportunityId());
            return;
        }

        Optional<LeadResponseDto> leadOpt = leadApi.findLeadById(event.getLeadId());
        if (leadOpt.isEmpty()) {
            log.warn("Lead ID {} not found for deal conversion", event.getLeadId());
            return;
        }

        LeadResponseDto lead = leadOpt.get();

        // 1. Perform deduplication & find/create CustomerAccount
        CustomerAccount account = customerDedupService.findOrCreateCustomerAccount(lead);

        // 2. Link Opportunity to CustomerAccount
        CustomerOpportunityLink link = CustomerOpportunityLink.builder()
                .customerAccountId(account.getId())
                .opportunityId(event.getOpportunityId())
                .build();
        linkRepository.save(link);

        // 3. Mark Lead as CONVERTED in Lead domain
        leadApi.updateStatus(lead.getId(), "CONVERTED");

        log.info("Successfully converted deal {} into Customer Account ID '{}' (Lead {} updated to CONVERTED)",
                event.getOpportunityId(), account.getId(), lead.getId());
    }
}
