package com.crm.task.api;

import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Published cross-domain interface for Task domain.
 * External domains MUST use this interface and DTOs rather than importing entity or repository classes directly.
 */
public interface TaskApi {

    List<TaskResponseDto> findTasksByRelatedObject(String relatedToType, UUID relatedToId);

    TaskResponseDto createTask(TaskCreateDto dto);

    long countTasksByStatus(String status);

    long countOverdueTasks();
}
