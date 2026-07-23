package com.crm.task.mapper;

import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskResponseDto;
import com.crm.task.api.dto.TaskUpdateDto;
import com.crm.task.entity.Task;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-23T13:58:22+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(TaskCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.assignedTo( dto.getAssignedTo() );
        task.description( dto.getDescription() );
        task.dueDate( dto.getDueDate() );
        task.priority( dto.getPriority() );
        task.relatedToId( dto.getRelatedToId() );
        task.relatedToType( dto.getRelatedToType() );
        task.status( dto.getStatus() );
        task.title( dto.getTitle() );
        task.type( dto.getType() );

        return task.build();
    }

    @Override
    public TaskResponseDto toDto(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskResponseDto.TaskResponseDtoBuilder taskResponseDto = TaskResponseDto.builder();

        taskResponseDto.assignedTo( task.getAssignedTo() );
        taskResponseDto.createdAt( task.getCreatedAt() );
        taskResponseDto.description( task.getDescription() );
        taskResponseDto.dueDate( task.getDueDate() );
        taskResponseDto.id( task.getId() );
        taskResponseDto.priority( task.getPriority() );
        taskResponseDto.relatedToId( task.getRelatedToId() );
        taskResponseDto.relatedToType( task.getRelatedToType() );
        taskResponseDto.status( task.getStatus() );
        taskResponseDto.title( task.getTitle() );
        taskResponseDto.type( task.getType() );
        taskResponseDto.updatedAt( task.getUpdatedAt() );

        return taskResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(TaskUpdateDto dto, Task entity) {
        if ( dto == null ) {
            return;
        }

        entity.setAssignedTo( dto.getAssignedTo() );
        entity.setDescription( dto.getDescription() );
        entity.setDueDate( dto.getDueDate() );
        entity.setPriority( dto.getPriority() );
        entity.setRelatedToId( dto.getRelatedToId() );
        entity.setRelatedToType( dto.getRelatedToType() );
        entity.setStatus( dto.getStatus() );
        entity.setTitle( dto.getTitle() );
        entity.setType( dto.getType() );
    }
}
