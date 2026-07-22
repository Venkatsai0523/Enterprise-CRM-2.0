package com.crm.notification.mapper;

import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.entity.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T19:30:59+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponseDto toDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponseDto.NotificationResponseDtoBuilder notificationResponseDto = NotificationResponseDto.builder();

        notificationResponseDto.id( notification.getId() );
        notificationResponseDto.recipientId( notification.getRecipientId() );
        notificationResponseDto.type( notification.getType() );
        notificationResponseDto.message( notification.getMessage() );
        notificationResponseDto.read( notification.isRead() );
        notificationResponseDto.createdAt( notification.getCreatedAt() );

        return notificationResponseDto.build();
    }
}
