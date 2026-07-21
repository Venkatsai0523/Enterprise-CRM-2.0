package com.crm.lead.entity;

import com.crm.lead.api.dto.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "lead_source", nullable = false, length = 50)
    private String leadSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Column(nullable = false)
    @Builder.Default
    private int score = 0;

    @Column(name = "assigned_rep_id")
    private UUID assignedRepId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
