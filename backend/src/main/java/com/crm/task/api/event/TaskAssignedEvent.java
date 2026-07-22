package com.crm.task.api.event;

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
public class TaskAssignedEvent {

    private UUID taskId;
    private UUID assignedTo;
    private String title;
    private Instant dueDate;
    private UUID organizationId;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
}
