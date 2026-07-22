package com.crm.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRuleCreateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Trigger event is required")
    private String triggerEvent;

    @NotBlank(message = "Conditions JSON is required")
    private String conditionsJson;

    @NotBlank(message = "Actions JSON is required")
    private String actionsJson;
}
