package com.crm.task.controller;

import com.crm.task.api.dto.*;
import com.crm.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management & Scheduling", description = "Endpoints for scheduling, assignment, and status updates of task activities")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Create a new task activity", description = "Creates a task, schedules it, validates references, and publishes a Kafka task.assigned event")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskCreateDto dto) {
        TaskResponseDto response = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Get task by ID", description = "Retrieves full details of a scheduled task")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id) {
        TaskResponseDto response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Search and filter tasks", description = "Retrieves paginated tasks filtered by assignee, status, and related entities. Max page size: 100.")
    public ResponseEntity<Page<TaskResponseDto>> getTasks(
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String relatedToType,
            @RequestParam(required = false) UUID relatedToId,
            @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Max(value = 100, message = "Page size must not exceed 100") @RequestParam(defaultValue = "10") int size
    ) {
        Page<TaskResponseDto> response = taskService.getTasksWithFilters(assignedTo, status, relatedToType, relatedToId, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Update task details", description = "Updates a task's parameters including title, due date, assignee, and description")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskUpdateDto dto) {
        TaskResponseDto response = taskService.updateTask(id, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Update task status", description = "Quick stage or status update for a task (e.g. mark COMPLETED)")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(@PathVariable UUID id, @RequestParam TaskStatus status) {
        TaskResponseDto response = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete task activity", description = "Permanently removes a scheduled task")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
