package com.crm.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRuleUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private boolean active;

    @NotBlank(message = "Conditions JSON is required")
    private String conditionsJson;

    @NotBlank(message = "Actions JSON is required")
    private String actionsJson;
}
