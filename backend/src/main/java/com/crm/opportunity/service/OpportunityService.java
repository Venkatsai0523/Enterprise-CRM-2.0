package com.crm.opportunity.service;

import com.crm.audit.api.AuditApi;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.lead.api.LeadApi;
import com.crm.opportunity.api.OpportunityApi;
import com.crm.opportunity.api.dto.*;
import com.crm.opportunity.api.event.DealLostEvent;
import com.crm.opportunity.api.event.DealWonEvent;
import com.crm.opportunity.entity.Opportunity;
import com.crm.opportunity.mapper.OpportunityMapper;
import com.crm.opportunity.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class OpportunityService implements OpportunityApi {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityMapper opportunityMapper;
    private final OpportunityStageValidator stageValidator;
    private final LeadApi leadApi;
    private final KafkaEventPublisher eventPublisher;
    private final AuditApi auditApi;

    public static final String TOPIC_DEAL_WON = "opportunity.deal-won";
    public static final String TOPIC_DEAL_LOST = "opportunity.deal-lost";

    @Transactional
    public OpportunityResponseDto createOpportunity(OpportunityCreateDto dto) {
        if (!leadApi.findLeadById(dto.getLeadId()).isPresent()) {
            throw new ResourceNotFoundException("Associated lead not found with ID: " + dto.getLeadId());
        }

        Opportunity opportunity = opportunityMapper.toEntity(dto);
        opportunity.setStage(OpportunityStage.PROSPECTING);

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        log.info("Created opportunity ID: '{}' for lead ID: '{}'", savedOpportunity.getId(), dto.getLeadId());

        auditApi.recordAudit("OPPORTUNITY", savedOpportunity.getId().toString(), "OPPORTUNITY_CREATED", "user", null, "PROSPECTING");

        return opportunityMapper.toDto(savedOpportunity);
    }

    @Transactional
    public OpportunityResponseDto updateStage(UUID opportunityId, OpportunityStageUpdateDto dto) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + opportunityId));

        stageValidator.validateTransition(opportunity.getStage(), dto.getStage(), dto.getLostReason());

        OpportunityStage oldStage = opportunity.getStage();
        opportunity.setStage(dto.getStage());

        if (dto.getStage() == OpportunityStage.WON) {
            opportunity.setClosedAt(Instant.now());
            opportunityRepository.save(opportunity);

            log.info("Opportunity '{}' marked as WON!", opportunityId);
            eventPublisher.publish(TOPIC_DEAL_WON, opportunityId.toString(), DealWonEvent.builder()
                    .opportunityId(opportunity.getId())
                    .leadId(opportunity.getLeadId())
                    .title(opportunity.getTitle())
                    .amount(opportunity.getEstimatedValue())
                    .organizationId(opportunity.getOrganizationId())
                    .build());

        } else if (dto.getStage() == OpportunityStage.LOST) {
            opportunity.setLostReason(dto.getLostReason());
            opportunity.setClosedAt(Instant.now());
            opportunityRepository.save(opportunity);

            log.info("Opportunity '{}' marked as LOST. Reason: '{}'", opportunityId, dto.getLostReason());
            eventPublisher.publish(TOPIC_DEAL_LOST, opportunityId.toString(), DealLostEvent.builder()
                    .opportunityId(opportunity.getId())
                    .leadId(opportunity.getLeadId())
                    .title(opportunity.getTitle())
                    .amount(opportunity.getEstimatedValue())
                    .lostReason(dto.getLostReason())
                    .organizationId(opportunity.getOrganizationId())
                    .build());
        } else {
            opportunityRepository.save(opportunity);
        }

        auditApi.recordAudit("OPPORTUNITY", opportunity.getId().toString(), "STAGE_CHANGE", "user", oldStage.name(), dto.getStage().name());

        log.info("Opportunity '{}' stage transitioned from {} -> {}", opportunityId, oldStage, dto.getStage());
        return opportunityMapper.toDto(opportunity);
    }

    @Transactional(readOnly = true)
    public OpportunityResponseDto getOpportunityById(UUID id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + id));
        return opportunityMapper.toDto(opportunity);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityResponseDto> getOpportunitiesWithFilters(OpportunityStage stage, UUID leadId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return opportunityRepository.findWithFilters(stage, leadId, pageRequest)
                .map(opportunityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<LostAnalysisDto> getLostAnalysis() {
        return opportunityRepository.aggregateLostAnalysis();
    }

    // --- OpportunityApi Implementation ---

    @Override
    @Transactional(readOnly = true)
    public Optional<OpportunityResponseDto> findOpportunityById(UUID opportunityId) {
        return opportunityRepository.findById(opportunityId).map(opportunityMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID opportunityId) {
        return opportunityRepository.existsById(opportunityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityResponseDto> findAllOpportunities() {
        return opportunityRepository.findAll().stream()
                .map(opportunityMapper::toDto)
                .collect(Collectors.toList());
    }
}
