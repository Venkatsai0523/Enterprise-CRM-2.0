package com.crm.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerOpportunityLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_account_id", nullable = false)
    private UUID customerAccountId;

    @Column(name = "opportunity_id", nullable = false, unique = true)
    private UUID opportunityId;

    @CreationTimestamp
    @Column(name = "linked_at", updatable = false)
    private Instant linkedAt;

    @org.hibernate.annotations.TenantId
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
