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
public class LeadScoredEvent {

    private UUID leadId;
    private String email;
    private String companyName;
    private int score;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
