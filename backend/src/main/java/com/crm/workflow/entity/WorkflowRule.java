package com.crm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import java.util.UUID;

@Entity
@Table(name = "workflow_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "trigger_event", nullable = false)
    private String triggerEvent;

    @Column(name = "conditions_json", nullable = false, columnDefinition = "TEXT")
    private String conditionsJson;

    @Column(name = "actions_json", nullable = false, columnDefinition = "TEXT")
    private String actionsJson;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @TenantId
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
