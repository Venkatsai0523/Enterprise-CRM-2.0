package com.crm.lead.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadAssignedEvent {

    private UUID leadId;
    private UUID assignedRepId;
    private int score;
    private UUID organizationId;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
