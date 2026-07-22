package com.crm.task.mapper;

import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskResponseDto;
import com.crm.task.api.dto.TaskUpdateDto;
import com.crm.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Task toEntity(TaskCreateDto dto);

    TaskResponseDto toDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(TaskUpdateDto dto, @MappingTarget Task entity);
}
