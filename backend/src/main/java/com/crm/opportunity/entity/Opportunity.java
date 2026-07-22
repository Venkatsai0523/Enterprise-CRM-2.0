package com.crm.opportunity.entity;

import com.crm.opportunity.api.dto.OpportunityStage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private OpportunityStage stage = OpportunityStage.PROSPECTING;

    @Column(name = "lost_reason")
    private String lostReason;

    @Column(name = "closed_at")
    private Instant closedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @org.hibernate.annotations.TenantId
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
