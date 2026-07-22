package com.crm.notification.api;

import com.crm.notification.api.dto.NotificationResponseDto;
import java.util.UUID;

/**
 * Published cross-domain interface for Notification domain.
 * External domains MUST use this interface and DTOs rather than importing entity or repository classes directly.
 */
public interface NotificationApi {
    NotificationResponseDto sendNotification(UUID recipientId, String type, String message);
}
