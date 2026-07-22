package com.crm.notification.service;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.entity.Notification;
import com.crm.notification.mapper.NotificationMapper;
import com.crm.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.crm.notification.api.NotificationApi;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class NotificationService implements NotificationApi {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsForRecipient(UUID recipientId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageRequest)
                .map(notificationMapper::toDto);
    }

    @Transactional
    public NotificationResponseDto markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification '{}' marked as read.", notificationId);
        return notificationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public NotificationResponseDto sendNotification(UUID recipientId, String type, String message) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .message(message)
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Notification sent to recipient: {} with type: {}", recipientId, type);
        return notificationMapper.toDto(saved);
    }
}
