package com.crm.lead.service;

import com.crm.audit.api.AuditApi;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.lead.api.LeadApi;
import com.crm.lead.api.dto.*;
import com.crm.lead.api.event.LeadScoredEvent;
import com.crm.lead.entity.Lead;
import com.crm.lead.mapper.LeadMapper;
import com.crm.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class LeadService implements LeadApi {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final LeadScoringService leadScoringService;
    private final KafkaEventPublisher eventPublisher;
    private final AuditApi auditApi;

    public static final String TOPIC_LEAD_SCORED = "lead.scored";

    @Transactional
    public LeadResponseDto createAndScoreLead(LeadCreateDto dto) {
        Lead lead = leadMapper.toEntity(dto);
        lead.setStatus(LeadStatus.NEW);

        // 1. Calculate score synchronously
        int score = leadScoringService.calculateScore(lead);
        lead.setScore(score);
        lead.setStatus(LeadStatus.SCORED);

        Lead savedLead = leadRepository.save(lead);
        log.info("Lead created and scored synchronously. Lead ID: {}, Score: {}", savedLead.getId(), score);

        // Record Audit
        auditApi.recordAudit("LEAD", savedLead.getId().toString(), "LEAD_CREATED", "system", null, "SCORED");

        // 2. Publish LeadScoredEvent to Kafka asynchronously
        LeadScoredEvent event = LeadScoredEvent.builder()
                .leadId(savedLead.getId())
                .email(savedLead.getEmail())
                .companyName(savedLead.getCompanyName())
                .score(score)
                .build();

        eventPublisher.publish(TOPIC_LEAD_SCORED, savedLead.getId().toString(), event);

        return leadMapper.toDto(savedLead);
    }

    @Transactional(readOnly = true)
    public LeadResponseDto getLeadById(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));
        return leadMapper.toDto(lead);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponseDto> getLeadsWithFilters(LeadStatus status, Integer minScore, Integer maxScore, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return leadRepository.findWithFilters(status, minScore, maxScore, pageRequest)
                .map(leadMapper::toDto);
    }

    // --- LeadApi Implementation ---

    @Override
    @Transactional(readOnly = true)
    public Optional<LeadResponseDto> findLeadById(UUID leadId) {
        return leadRepository.findById(leadId).map(leadMapper::toDto);
    }

    @Override
    @Transactional
    public boolean updateStatus(UUID leadId, String newStatus) {
        return leadRepository.findById(leadId).map(lead -> {
            String oldStatus = lead.getStatus() != null ? lead.getStatus().name() : null;
            lead.setStatus(LeadStatus.valueOf(newStatus));
            leadRepository.save(lead);
            auditApi.recordAudit("LEAD", lead.getId().toString(), "STATUS_CHANGE", "system", oldStatus, newStatus);
            return true;
        }).orElse(false);
    }
}
