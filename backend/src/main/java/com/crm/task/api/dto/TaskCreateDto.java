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
public class TaskCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Instant dueDate;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskType type = TaskType.TASK;

    @NotNull(message = "Assigned to user ID is required")
    private UUID assignedTo;

    private String relatedToType; // e.g. "LEAD" or "OPPORTUNITY"

    private UUID relatedToId;
}
