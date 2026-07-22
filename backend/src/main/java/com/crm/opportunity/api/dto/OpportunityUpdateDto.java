package com.crm.opportunity.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityUpdateDto {

    @NotBlank(message = "Opportunity title is required")
    private String title;

    @NotNull(message = "Estimated value is required")
    @DecimalMin(value = "0.0", message = "Estimated value must be non-negative")
    private BigDecimal estimatedValue;

    private OpportunityStage stage;

    private String lostReason;
}
