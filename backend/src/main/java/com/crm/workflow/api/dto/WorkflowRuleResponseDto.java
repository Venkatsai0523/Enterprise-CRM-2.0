package com.crm.workflow.api.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRuleResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String triggerEvent;
    private String conditionsJson;
    private String actionsJson;
    private boolean active;
    private UUID organizationId;
}
