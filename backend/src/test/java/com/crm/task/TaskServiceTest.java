package com.crm.task;

import com.crm.audit.api.AuditApi;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.identity.api.IdentityApi;
import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.lead.api.LeadApi;
import com.crm.lead.api.dto.LeadResponseDto;
import com.crm.opportunity.api.OpportunityApi;
import com.crm.task.api.dto.*;
import com.crm.task.entity.Task;
import com.crm.task.mapper.TaskMapper;
import com.crm.task.repository.TaskRepository;
import com.crm.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private IdentityApi identityApi;

    @Mock
    private LeadApi leadApi;

    @Mock
    private OpportunityApi opportunityApi;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @Mock
    private AuditApi auditApi;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Create Task: Success with valid assignee and lead")
    void createTask_Success() {
        UUID assignedRepId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        TaskCreateDto dto = TaskCreateDto.builder()
                .title("Call Lead")
                .assignedTo(assignedRepId)
                .relatedToType("LEAD")
                .relatedToId(leadId)
                .build();

        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title(dto.getTitle())
                .assignedTo(assignedRepId)
                .relatedToType("LEAD")
                .relatedToId(leadId)
                .status(TaskStatus.TODO)
                .build();

        TaskResponseDto expectedResponse = TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .assignedTo(assignedRepId)
                .status(TaskStatus.TODO)
                .build();

        when(identityApi.existsById(assignedRepId)).thenReturn(true);
        when(leadApi.findLeadById(leadId)).thenReturn(Optional.of(new LeadResponseDto()));
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(expectedResponse);

        TaskResponseDto actualResponse = taskService.createTask(dto);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getTitle()).isEqualTo("Call Lead");
        verify(eventPublisher).publish(any(), any(), any());
        verify(auditApi).recordAudit(eq("TASK"), eq(task.getId().toString()), eq("TASK_CREATED"), any(), any(), eq("TODO"));
    }

    @Test
    @DisplayName("Create Task: Fails when assigned user does not exist")
    void createTask_InvalidAssignee() {
        UUID assignedRepId = UUID.randomUUID();
        TaskCreateDto dto = TaskCreateDto.builder()
                .title("Call Lead")
                .assignedTo(assignedRepId)
                .build();

        when(identityApi.existsById(assignedRepId)).thenReturn(false);

        assertThatThrownBy(() -> taskService.createTask(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Assigned user not found with ID");

        verifyNoInteractions(taskRepository, eventPublisher, auditApi);
    }

    @Test
    @DisplayName("Create Task: Fails when related Lead does not exist")
    void createTask_InvalidLead() {
        UUID assignedRepId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        TaskCreateDto dto = TaskCreateDto.builder()
                .title("Call Lead")
                .assignedTo(assignedRepId)
                .relatedToType("LEAD")
                .relatedToId(leadId)
                .build();

        when(identityApi.existsById(assignedRepId)).thenReturn(true);
        when(leadApi.findLeadById(leadId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Related Lead not found");

        verifyNoInteractions(taskRepository, eventPublisher, auditApi);
    }

    @Test
    @DisplayName("Update Task Status: Success and records audit log")
    void updateTaskStatus_Success() {
        UUID taskId = UUID.randomUUID();
        Task task = Task.builder()
                .id(taskId)
                .title("Call Lead")
                .assignedTo(UUID.randomUUID())
                .status(TaskStatus.TODO)
                .build();

        TaskResponseDto expectedResponse = TaskResponseDto.builder()
                .id(taskId)
                .title("Call Lead")
                .status(TaskStatus.COMPLETED)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(expectedResponse);

        TaskResponseDto actualResponse = taskService.updateTaskStatus(taskId, TaskStatus.COMPLETED);

        assertThat(actualResponse.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(auditApi).recordAudit("TASK", taskId.toString(), "STATUS_CHANGE", "user", "TODO", "COMPLETED");
    }
}
