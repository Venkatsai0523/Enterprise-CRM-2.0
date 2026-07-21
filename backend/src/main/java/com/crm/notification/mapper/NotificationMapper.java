package com.crm.notification.mapper;

import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDto toDto(Notification notification);
}
