package com.crm.task.service;

import com.crm.audit.api.AuditApi;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.common.exception.BadRequestException;
import com.crm.identity.api.IdentityApi;
import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.LeadApi;
import com.crm.opportunity.api.OpportunityApi;
import com.crm.task.api.TaskApi;
import com.crm.task.api.dto.*;
import com.crm.task.api.event.TaskAssignedEvent;
import com.crm.task.entity.Task;
import com.crm.task.mapper.TaskMapper;
import com.crm.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class TaskService implements TaskApi {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final IdentityApi identityApi;
    private final LeadApi leadApi;
    private final OpportunityApi opportunityApi;
    private final KafkaEventPublisher eventPublisher;
    private final AuditApi auditApi;

    @Transactional
    public TaskResponseDto createTask(TaskCreateDto dto) {
        validateAssignedUser(dto.getAssignedTo());
        validateRelatedObject(dto.getRelatedToType(), dto.getRelatedToId());

        Task task = taskMapper.toEntity(dto);
        Task savedTask = taskRepository.save(task);
        log.info("Created task ID: '{}' assigned to: '{}'", savedTask.getId(), savedTask.getAssignedTo());

        // Publish task assignment event
        publishTaskAssigned(savedTask);

        // Record audit
        auditApi.recordAudit("TASK", savedTask.getId().toString(), "TASK_CREATED", "user", null,
                savedTask.getStatus().name());

        return taskMapper.toDto(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        return taskMapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksWithFilters(UUID assignedTo, TaskStatus status, String relatedToType,
            UUID relatedToId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskRepository.findWithFilters(assignedTo, status, relatedToType, relatedToId, pageRequest)
                .map(taskMapper::toDto);
    }

    @Transactional
    public TaskResponseDto updateTask(UUID id, TaskUpdateDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        validateAssignedUser(dto.getAssignedTo());
        validateRelatedObject(dto.getRelatedToType(), dto.getRelatedToId());

        UUID oldAssignee = task.getAssignedTo();
        TaskStatus oldStatus = task.getStatus();

        taskMapper.updateEntityFromDto(dto, task);
        Task updatedTask = taskRepository.save(task);
        log.info("Updated task ID: '{}'", updatedTask.getId());

        // If assignee changed, publish new assignment event
        if (!dto.getAssignedTo().equals(oldAssignee)) {
            publishTaskAssigned(updatedTask);
        }

        // Record audit logs
        if (dto.getStatus() != oldStatus) {
            auditApi.recordAudit("TASK", updatedTask.getId().toString(), "STATUS_CHANGE", "user", oldStatus.name(),
                    dto.getStatus().name());
        }
        auditApi.recordAudit("TASK", updatedTask.getId().toString(), "TASK_UPDATED", "user", null, null);

        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(UUID id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        log.info("Updated status for task ID: '{}' from {} to {}", id, oldStatus, status);

        auditApi.recordAudit("TASK", updatedTask.getId().toString(), "STATUS_CHANGE", "user", oldStatus.name(),
                status.name());

        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        taskRepository.delete(task);
        log.info("Deleted task ID: '{}'", id);

        auditApi.recordAudit("TASK", id.toString(), "TASK_DELETED", "user", task.getStatus().name(), null);
    }

    // --- TaskApi implementation ---
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> findTasksByRelatedObject(String relatedToType, UUID relatedToId) {
        return taskRepository.findByRelatedToTypeAndRelatedToId(relatedToType, relatedToId).stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- Validation Helpers ---
    private void validateAssignedUser(UUID userId) {
        if (userId != null && !identityApi.existsById(userId)) {
            throw new ResourceNotFoundException("Assigned user not found with ID: " + userId);
        }
    }

    private void validateRelatedObject(String relatedToType, UUID relatedToId) {
        if (relatedToType == null || relatedToId == null) {
            return;
        }

        if ("LEAD".equalsIgnoreCase(relatedToType)) {
            if (!leadApi.findLeadById(relatedToId).isPresent()) {
                throw new ResourceNotFoundException("Related Lead not found with ID: " + relatedToId);
            }
        } else if ("OPPORTUNITY".equalsIgnoreCase(relatedToType)) {
            if (!opportunityApi.existsById(relatedToId)) {
                throw new ResourceNotFoundException("Related Opportunity not found with ID: " + relatedToId);
            }
        } else {
            throw new BadRequestException("Invalid relatedToType: " + relatedToType + ". Must be LEAD or OPPORTUNITY");
        }
    }

    private void publishTaskAssigned(Task task) {
        eventPublisher.publish(
                KafkaTopicConfig.TOPIC_TASK_ASSIGNED,
                task.getId().toString(),
                TaskAssignedEvent.builder()
                        .taskId(task.getId())
                        .assignedTo(task.getAssignedTo())
                        .title(task.getTitle())
                        .dueDate(task.getDueDate())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTasksByStatus(String status) {
        return taskRepository.countByStatus(TaskStatus.valueOf(status.toUpperCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public long countOverdueTasks() {
        return taskRepository.countOverdueTasks(java.time.Instant.now());
    }
}
