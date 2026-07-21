package com.crm.opportunity.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityStageUpdateDto {

    @NotNull(message = "Target stage is required")
    private OpportunityStage stage;

    private String lostReason; // Required if stage is LOST
}
