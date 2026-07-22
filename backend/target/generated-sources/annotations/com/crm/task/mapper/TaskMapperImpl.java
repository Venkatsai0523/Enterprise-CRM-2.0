package com.crm.task.mapper;

import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskResponseDto;
import com.crm.task.api.dto.TaskUpdateDto;
import com.crm.task.entity.Task;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T20:23:30+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(TaskCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.title( dto.getTitle() );
        task.description( dto.getDescription() );
        task.dueDate( dto.getDueDate() );
        task.priority( dto.getPriority() );
        task.status( dto.getStatus() );
        task.type( dto.getType() );
        task.assignedTo( dto.getAssignedTo() );
        task.relatedToType( dto.getRelatedToType() );
        task.relatedToId( dto.getRelatedToId() );

        return task.build();
    }

    @Override
    public TaskResponseDto toDto(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskResponseDto.TaskResponseDtoBuilder taskResponseDto = TaskResponseDto.builder();

        taskResponseDto.id( task.getId() );
        taskResponseDto.title( task.getTitle() );
        taskResponseDto.description( task.getDescription() );
        taskResponseDto.dueDate( task.getDueDate() );
        taskResponseDto.priority( task.getPriority() );
        taskResponseDto.status( task.getStatus() );
        taskResponseDto.type( task.getType() );
        taskResponseDto.assignedTo( task.getAssignedTo() );
        taskResponseDto.relatedToType( task.getRelatedToType() );
        taskResponseDto.relatedToId( task.getRelatedToId() );
        taskResponseDto.createdAt( task.getCreatedAt() );
        taskResponseDto.updatedAt( task.getUpdatedAt() );

        return taskResponseDto.build();
    }

    @Override
    public void updateEntityFromDto(TaskUpdateDto dto, Task entity) {
        if ( dto == null ) {
            return;
        }

        entity.setTitle( dto.getTitle() );
        entity.setDescription( dto.getDescription() );
        entity.setDueDate( dto.getDueDate() );
        entity.setPriority( dto.getPriority() );
        entity.setStatus( dto.getStatus() );
        entity.setType( dto.getType() );
        entity.setAssignedTo( dto.getAssignedTo() );
        entity.setRelatedToType( dto.getRelatedToType() );
        entity.setRelatedToId( dto.getRelatedToId() );
    }
}
