package com.crm.lead.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponseDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String companyName;
    private String companySize;
    private String leadSource;
    private LeadStatus status;
    private int score;
    private UUID assignedRepId;
    private Instant createdAt;
    private Instant updatedAt;
}
