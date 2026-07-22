package com.crm.task.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TaskUpdateDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Instant dueDate;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Type is required")
    private TaskType type;

    @NotNull(message = "Assigned to user ID is required")
    private UUID assignedTo;

    private String relatedToType;

    private UUID relatedToId;
}
