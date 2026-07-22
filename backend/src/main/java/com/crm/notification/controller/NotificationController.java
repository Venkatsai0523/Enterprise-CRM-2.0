package com.crm.notification.controller;

import com.crm.notification.api.dto.NotificationResponseDto;
import com.crm.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user notifications generated from real-time domain events")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for a recipient ID. Max page size: 100.")
    public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Max(value = 100, message = "Page size must not exceed 100") @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationResponseDto> response = notificationService.getNotificationsForRecipient(recipientId, page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Mark notification as read", description = "Updates status of notification to read")
    public ResponseEntity<NotificationResponseDto> markAsRead(@PathVariable UUID id) {
        NotificationResponseDto response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }
}
