package com.crm.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "old_state", columnDefinition = "TEXT")
    private String oldState;

    @Column(name = "new_state", columnDefinition = "TEXT")
    private String newState;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @org.hibernate.annotations.TenantId
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
