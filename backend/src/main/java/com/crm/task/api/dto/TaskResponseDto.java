package com.crm.task.api.dto;

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
public class TaskResponseDto {

    private UUID id;
    private String title;
    private String description;
    private Instant dueDate;
    private TaskPriority priority;
    private TaskStatus status;
    private TaskType type;
    private UUID assignedTo;
    private String relatedToType;
    private UUID relatedToId;
    private Instant createdAt;
    private Instant updatedAt;
}
