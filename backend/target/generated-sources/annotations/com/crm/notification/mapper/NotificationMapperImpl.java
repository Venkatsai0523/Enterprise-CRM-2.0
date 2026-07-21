package com.crm.notification.mapper;

import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.entity.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T00:13:00+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponseDto toDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponseDto.NotificationResponseDtoBuilder notificationResponseDto = NotificationResponseDto.builder();

        notificationResponseDto.createdAt( notification.getCreatedAt() );
        notificationResponseDto.id( notification.getId() );
        notificationResponseDto.message( notification.getMessage() );
        notificationResponseDto.read( notification.isRead() );
        notificationResponseDto.recipientId( notification.getRecipientId() );
        notificationResponseDto.type( notification.getType() );

        return notificationResponseDto.build();
    }
}
