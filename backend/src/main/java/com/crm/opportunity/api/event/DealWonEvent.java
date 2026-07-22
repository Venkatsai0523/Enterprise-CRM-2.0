package com.crm.opportunity.api.event;

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
public class DealWonEvent {

    private UUID opportunityId;
    private UUID leadId;
    private String title;
    private BigDecimal amount;
    private UUID organizationId;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
