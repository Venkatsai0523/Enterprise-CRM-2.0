package com.crm.opportunity.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityResponseDto {

    private UUID id;
    private String title;
    private UUID leadId;
    private BigDecimal estimatedValue;
    private OpportunityStage stage;
    private String lostReason;
    private Instant closedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
